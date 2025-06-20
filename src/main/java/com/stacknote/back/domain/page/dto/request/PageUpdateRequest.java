package com.stacknote.back.domain.page.dto.request;

import com.stacknote.back.domain.page.entity.Page;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 페이지 수정 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PageUpdateRequest {

    @Size(min = 1, max = 200, message = "제목은 1자 이상 200자 이하여야 합니다.")
    private String title;

    private String content; // 마크다운 콘텐츠

    @Size(max = 10, message = "아이콘은 10자를 초과할 수 없습니다.")
    @Pattern(regexp = "^[\\p{So}\\p{Sk}]*$", message = "아이콘은 이모지만 입력 가능합니다.")
    private String icon;

    @Size(max = 500, message = "커버 이미지 URL은 500자를 초과할 수 없습니다.")
    private String coverImageUrl;

    private Page.PageType pageType;

    private Boolean isPublished;

    private Boolean isTemplate;

    private Boolean isLocked;

    private Integer sortOrder;
}