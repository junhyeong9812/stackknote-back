package com.stacknote.back.domain.page.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 페이지 이동 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PageMoveRequest {

    private Long newParentId; // 새로운 부모 페이지 ID (null이면 최상위로 이동)

    private Integer newSortOrder; // 새로운 정렬 순서
}