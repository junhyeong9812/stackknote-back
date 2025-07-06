package com.stacknote.back.domain.search.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 전역 검색 결과 응답 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "전역 검색 결과")
public class GlobalSearchResponse {

    @Schema(description = "검색 결과 그룹 목록")
    @Builder.Default
    private List<SearchResultGroup> results = new ArrayList<>();

    @Schema(description = "전체 검색 결과 수")
    private int totalCount;
}