package com.stacknote.back.domain.page.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 페이지 복제 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PageDuplicateRequest {

    @Size(min = 1, max = 200, message = "제목은 1자 이상 200자 이하여야 합니다.")
    private String newTitle; // 새로운 제목 (null이면 "Copy of 원본제목")

    private Long newParentId; // 복제할 부모 페이지 ID (null이면 원본과 같은 부모)

    private Boolean includeChildren = false; // 자식 페이지도 함께 복제할지 여부

    private Boolean resetViewCount = true; // 조회수 초기화 여부
}