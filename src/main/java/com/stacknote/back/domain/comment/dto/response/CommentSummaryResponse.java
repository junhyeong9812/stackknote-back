package com.stacknote.back.domain.comment.dto.response;

import com.stacknote.back.domain.comment.entity.Comment;
import com.stacknote.back.global.utils.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 댓글 요약 응답 DTO (목록용)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentSummaryResponse {

    private Long id;
    private String content;
    private String contentPreview; // 내용 미리보기 (50자)
    private Long pageId;
    private String pageTitle;
    private String authorName;
    private String authorProfileImage;
    private Boolean isEdited;
    private Integer likesCount;
    private Integer replyCount;
    private String relativeTime;
    private LocalDateTime createdAt;

    /**
     * Comment 엔티티로부터 CommentSummaryResponse 생성
     */
    public static CommentSummaryResponse from(Comment comment) {
        String contentPreview = comment.getContent();
        if (contentPreview.length() > 50) {
            contentPreview = contentPreview.substring(0, 50) + "...";
        }

        return CommentSummaryResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .contentPreview(contentPreview)
                .pageId(comment.getPage().getId())
                .pageTitle(comment.getPage().getTitle())
                .authorName(comment.getAuthor().getUsername())
                .authorProfileImage(comment.getAuthor().getProfileImageUrl())
                .isEdited(comment.getIsEdited())
                .likesCount(comment.getLikesCount())
                .replyCount(comment.getReplyCount())
                .relativeTime(DateUtil.getRelativeTime(comment.getCreatedAt()))
                .createdAt(comment.getCreatedAt())
                .build();
    }
}