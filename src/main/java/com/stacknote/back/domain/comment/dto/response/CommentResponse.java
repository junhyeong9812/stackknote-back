package com.stacknote.back.domain.comment.dto.response;

import com.stacknote.back.domain.comment.entity.Comment;
import com.stacknote.back.domain.user.dto.response.UserResponse;
import com.stacknote.back.global.utils.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 댓글 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private String content;
    private Long pageId;
    private String pageTitle;
    private UserResponse author;
    private Long parentId;
    private List<CommentResponse> replies;
    private Boolean isEdited;
    private Integer likesCount;
    private String mentions;
    private Integer depth;
    private Integer replyCount;
    private String relativeTime; // "5분 전" 형태
    private Boolean canEdit;
    private Boolean canDelete;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Comment 엔티티로부터 CommentResponse 생성
     */
    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .pageId(comment.getPage().getId())
                .pageTitle(comment.getPage().getTitle())
                .author(UserResponse.from(comment.getAuthor()))
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .replies(comment.getReplies().stream()
                        .filter(reply -> !reply.isDeleted())
                        .map(CommentResponse::fromSummary)
                        .collect(Collectors.toList()))
                .isEdited(comment.getIsEdited())
                .likesCount(comment.getLikesCount())
                .mentions(comment.getMentions())
                .depth(comment.getDepth())
                .replyCount(comment.getReplyCount())
                .relativeTime(DateUtil.getRelativeTime(comment.getCreatedAt()))
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    /**
     * 권한 정보와 함께 CommentResponse 생성
     */
    public static CommentResponse fromWithPermissions(Comment comment, Long currentUserId) {
        CommentResponse response = from(comment);

        if (currentUserId != null) {
            response.canEdit = comment.getAuthor().getId().equals(currentUserId);
            response.canDelete = comment.getAuthor().getId().equals(currentUserId);
        } else {
            response.canEdit = false;
            response.canDelete = false;
        }

        return response;
    }

    /**
     * 대댓글용 간단한 응답 생성 (replies 필드 제외)
     */
    public static CommentResponse fromSummary(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .pageId(comment.getPage().getId())
                .pageTitle(comment.getPage().getTitle())
                .author(UserResponse.from(comment.getAuthor()))
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .isEdited(comment.getIsEdited())
                .likesCount(comment.getLikesCount())
                .mentions(comment.getMentions())
                .depth(comment.getDepth())
                .replyCount(comment.getReplyCount())
                .relativeTime(DateUtil.getRelativeTime(comment.getCreatedAt()))
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}