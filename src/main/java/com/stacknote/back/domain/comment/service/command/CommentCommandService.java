package com.stacknote.back.domain.comment.service.command;

import com.stacknote.back.domain.comment.dto.request.CommentCreateRequest;
import com.stacknote.back.domain.comment.dto.request.CommentUpdateRequest;
import com.stacknote.back.domain.comment.dto.response.CommentResponse;
import com.stacknote.back.domain.comment.entity.Comment;
import com.stacknote.back.domain.comment.exception.CommentAccessDeniedException;
import com.stacknote.back.domain.comment.exception.CommentNotFoundException;
import com.stacknote.back.domain.comment.exception.InvalidCommentException;
import com.stacknote.back.domain.comment.repository.CommentRepository;
import com.stacknote.back.domain.page.entity.Page;
import com.stacknote.back.domain.page.repository.PageRepository;
import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.global.exception.custom.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 댓글 명령 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommentCommandService {

    private final CommentRepository commentRepository;
    private final PageRepository pageRepository;

    /**
     * 댓글 생성
     */
    public CommentResponse createComment(CommentCreateRequest request, User author) {
        // 페이지 존재 확인
        Page page = pageRepository.findById(request.getPageId())
                .orElseThrow(() -> new EntityNotFoundException("페이지를 찾을 수 없습니다."));

        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findActiveCommentById(request.getParentId())
                    .orElseThrow(() -> new CommentNotFoundException(request.getParentId()));

            // 부모 댓글과 같은 페이지인지 확인
            if (!parent.getPage().getId().equals(page.getId())) {
                throw new InvalidCommentException("부모 댓글과 같은 페이지에만 대댓글을 작성할 수 있습니다.");
            }
        }

        Comment comment = Comment.builder()
                .content(request.getContent())
                .page(page)
                .author(author)
                .parent(parent)
                .mentions(request.getMentions())
                .build();

        Comment savedComment = commentRepository.save(comment);

        // 부모 댓글이 있다면 부모 댓글에 대댓글 추가
        if (parent != null) {
            parent.addReply(savedComment);
        }

        log.info("댓글 생성 완료 - ID: {}, 작성자: {}, 페이지: {}",
                savedComment.getId(), author.getEmail(), page.getId());

        return CommentResponse.fromWithPermissions(savedComment, author.getId());
    }

    /**
     * 댓글 수정
     */
    public CommentResponse updateComment(Long commentId, CommentUpdateRequest request, User user) {
        Comment comment = commentRepository.findActiveCommentById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        // 작성자 권한 확인
        if (!comment.canEdit(user)) {
            throw new CommentAccessDeniedException("댓글을 수정할 권한이 없습니다.");
        }

        comment.updateContent(request.getContent());
        comment.setMentions(request.getMentions());

        log.info("댓글 수정 완료 - ID: {}, 수정자: {}", commentId, user.getEmail());

        return CommentResponse.fromWithPermissions(comment, user.getId());
    }

    /**
     * 댓글 삭제 (소프트 삭제)
     */
    public void deleteComment(Long commentId, User user) {
        Comment comment = commentRepository.findActiveCommentById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        // 삭제 권한 확인
        if (!comment.canDelete(user)) {
            throw new CommentAccessDeniedException("댓글을 삭제할 권한이 없습니다.");
        }

        // 대댓글이 있는 경우 대댓글도 함께 삭제
        deleteCommentWithReplies(comment);

        log.info("댓글 삭제 완료 - ID: {}, 삭제자: {}", commentId, user.getEmail());
    }

    /**
     * 댓글과 모든 대댓글 삭제
     */
    private void deleteCommentWithReplies(Comment comment) {
        // 모든 대댓글 삭제
        for (Comment reply : comment.getReplies()) {
            if (!reply.isDeleted()) {
                deleteCommentWithReplies(reply); // 재귀적으로 삭제
            }
        }

        // 댓글 소프트 삭제
        comment.markAsDeleted();
    }

    /**
     * 댓글 좋아요 증가
     */
    public void incrementLikes(Long commentId, User user) {
        Comment comment = commentRepository.findActiveCommentById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        comment.incrementLikes();

        log.debug("댓글 좋아요 증가 - ID: {}, 사용자: {}", commentId, user.getEmail());
    }

    /**
     * 댓글 좋아요 감소
     */
    public void decrementLikes(Long commentId, User user) {
        Comment comment = commentRepository.findActiveCommentById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        comment.decrementLikes();

        log.debug("댓글 좋아요 감소 - ID: {}, 사용자: {}", commentId, user.getEmail());
    }

    /**
     * 페이지의 모든 댓글 삭제 (페이지 삭제 시)
     */
    public void deleteCommentsByPage(Page page) {
        int deletedCount = commentRepository.softDeleteCommentsByPage(page);
        log.info("페이지 댓글 삭제 완료 - 페이지 ID: {}, 삭제된 댓글 수: {}", page.getId(), deletedCount);
    }

    /**
     * 사용자의 모든 댓글 삭제 (계정 삭제 시)
     */
    public void deleteCommentsByAuthor(User author) {
        int deletedCount = commentRepository.softDeleteCommentsByAuthor(author);
        log.info("사용자 댓글 삭제 완료 - 사용자: {}, 삭제된 댓글 수: {}", author.getEmail(), deletedCount);
    }

    /**
     * 댓글 검증
     */
    private void validateComment(CommentCreateRequest request) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new InvalidCommentException("댓글 내용은 필수입니다.");
        }

        if (request.getContent().length() > 1000) {
            throw new InvalidCommentException("댓글은 1000자를 초과할 수 없습니다.");
        }
    }
}