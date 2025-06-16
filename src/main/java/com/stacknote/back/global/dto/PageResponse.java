package com.stacknote.back.global.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 페이징 응답 DTO
 * 페이징 처리된 데이터와 메타정보를 포함
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> content;          // 실제 데이터 목록
    private int page;                 // 현재 페이지 번호 (0부터 시작)
    private int size;                 // 페이지 크기
    private long totalElements;       // 전체 요소 수
    private int totalPages;           // 전체 페이지 수
    private boolean first;            // 첫 번째 페이지 여부
    private boolean last;             // 마지막 페이지 여부
    private boolean hasNext;          // 다음 페이지 존재 여부
    private boolean hasPrevious;      // 이전 페이지 존재 여부

    // Spring Data Page 객체로부터 생성
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}