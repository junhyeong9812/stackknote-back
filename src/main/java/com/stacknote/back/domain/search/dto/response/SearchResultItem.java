package com.stacknote.back.domain.search.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 검색 결과 항목
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "검색 결과 항목")
public class SearchResultItem {

    @Schema(description = "항목 ID")
    private Long id;

    @Schema(description = "항목 타입", allowableValues = {"PAGE", "WORKSPACE"})
    private String type;

    @Schema(description = "제목")
    private String title;

    @Schema(description = "아이콘")
    private String icon;

    @Schema(description = "검색 하이라이트 (검색어가 포함된 부분)")
    private String highlight;

    @Schema(description = "페이지 경로 (계층 구조)", example = "워크스페이스 > 부모 페이지 > 현재 페이지")
    private String path;
}