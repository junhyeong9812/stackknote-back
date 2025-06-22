package com.stacknote.back.domain.comment.service.query;

import com.stacknote.back.domain.comment.dto.response.CommentResponse;
import com.stacknote.back.domain.comment.dto.response.CommentSummaryResponse;
import com.stacknote.back.domain.comment.entity.Comment;
import com.stacknote.back.domain.comment.exception.CommentNotFoundException;
import com.stacknote.back.domain.comment.repository.CommentRepository;
import com.stacknote.back.domain.page.entity.Page;
import com.stacknote.back.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 댓글 조회 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService {

    private final CommentRepository commentRepository;

    /**
     * 댓글 ID로 댓글 조회
     */
    public CommentResponse getCommentById(Long commentId, Long currentUserId) {
        Comment comment = commentRepository.findActiveCommentById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        return CommentResponse.fromWithPermissions(comment, currentUserId);
    }

    /**
     * 페이지의 최상위 댓글 목록 조회
     */
    public List<CommentResponse> getRootCommentsByPage(Page targetPage, Long currentUserId) {
        List<Comment> comments = commentRepository.findRootCommentsByPage(targetPage);

        return comments.stream()
                .map(comment -> CommentResponse.fromWithPermissions(comment, currentUserId))
                .collect(Collectors.toList());
    }

    /**
     * 페이지의 최상위 댓글 목록 조회 (페이징)
     */
    public List<CommentResponse> getRootCommentsByPage(Page targetPage, int page, int size, Long currentUserId) {
        Pageable pageable = PageRequest.of(page, size);
        List<Comment> comments = commentRepository.findRootCommentsByPage(targetPage, pageable);

        return comments.stream()
                .map(comment -> CommentResponse.fromWithPermissions(comment, currentUserId))
                .collect(Collectors.toList());
    }

    /**
     * 특정 댓글의 대댓글 조회
     */
    public List<CommentResponse> getRepliesByParent(Long parentId, Long currentUserId) {
        Comment parent = commentRepository.findActiveCommentById(parentId)
                .orElseThrow(() -> new CommentNotFoundException(parentId));

        List<Comment> replies = commentRepository.findRepliesByParent(parent);

        return replies.stream()
                .map(reply -> CommentResponse.fromWithPermissions(reply, currentUserId))
                .collect(Collectors.toList());
    }

    /**
     * 페이지의 전체 댓글 수 조회
     */
    public long getCommentCountByPage(Page targetPage) {
        return commentRepository.countCommentsByPage(targetPage);
    }

    /**
     * 페이지의 최상위 댓글 수 조회
     */
    public long getRootCommentCountByPage(Page targetPage) {
        return commentRepository.countRootCommentsByPage(targetPage);
    }

    /**
     * 사용자가 작성한 댓글 조회
     */
    public List<CommentSummaryResponse> getCommentsByAuthor(User author, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Comment> comments = commentRepository.findCommentsByAuthor(author, pageable);

        return comments.stream()
                .map(CommentSummaryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자가 작성한 댓글 수 조회
     */
    public long getCommentCountByAuthor(User author) {
        return commentRepository.countCommentsByAuthor(author);
    }

    /**
     * 특정 기간 내 댓글 조회
     */
    public List<CommentSummaryResponse> getCommentsByDateRange(
            Page targetPage, LocalDateTime startDate, LocalDateTime endDate) {
        List<Comment> comments = commentRepository.findCommentsByPageAndDateRange(targetPage, startDate, endDate);

        return comments.stream()
                .map(CommentSummaryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 최근 댓글 조회
     */
    public List<CommentSummaryResponse> getRecentComments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Comment> comments = commentRepository.findRecentComments(pageable);

        return comments.stream()
                .map(CommentSummaryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 워크스페이스의 최근 댓글 조회
     */
    public List<CommentSummaryResponse> getRecentCommentsByWorkspace(Long workspaceId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Comment> comments = commentRepository.findRecentCommentsByWorkspace(workspaceId, pageable);

        return comments.stream()
                .map(CommentSummaryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 인기 댓글 조회 (좋아요 수 기준)
     */
    public List<CommentResponse> getPopularCommentsByPage(Page targetPage, int pageNum, int size, Long currentUserId) {
        Pageable pageable = PageRequest.of(pageNum, size);
        List<Comment> comments = commentRepository.findPopularCommentsByPage(targetPage, pageable);

        return comments.stream()
                .map(comment -> CommentResponse.fromWithPermissions(comment, currentUserId))
                .collect(Collectors.toList());
    }

    /**
     * 멘션이 포함된 댓글 조회
     */
    public List<CommentSummaryResponse> getCommentsWithMentions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Comment> comments = commentRepository.findCommentsWithMentions(pageable);

        return comments.stream()
                .map(CommentSummaryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자를 멘션한 댓글 조회
     */
    public List<CommentSummaryResponse> getCommentsByMentionedUser(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Comment> comments = commentRepository.findCommentsByMentionedUser(username, pageable);

        return comments.stream()
                .map(CommentSummaryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 댓글 내용으로 검색
     */
    public List<CommentResponse> searchCommentsByContent(Page targetPage, String keyword, Long currentUserId) {
        List<Comment> comments = commentRepository.searchCommentsByContent(targetPage, keyword);

        return comments.stream()
                .map(comment -> CommentResponse.fromWithPermissions(comment, currentUserId))
                .collect(Collectors.toList());
    }

    /**
     * 댓글이 존재하는지 확인
     */
    public boolean existsComment(Long commentId) {
        return commentRepository.findActiveCommentById(commentId).isPresent();
    }

    /**
     * 사용자가 댓글 작성자인지 확인
     */
    public boolean isCommentAuthor(Long commentId, User user) {
        Comment comment = commentRepository.findActiveCommentById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        return comment.isAuthor(user);
    }
}