package com.stacknote.back.domain.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 댓글 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequest {

    @NotNull(message = "페이지 ID는 필수입니다.")
    private Long pageId;

    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Size(min = 1, max = 1000, message = "댓글은 1자 이상 1000자 이하여야 합니다.")
    private String content;

    private Long parentId; // 대댓글인 경우 부모 댓글 ID

    @Size(max = 500, message = "멘션 정보는 500자를 초과할 수 없습니다.")
    private String mentions; // 멘션된 사용자 정보
}