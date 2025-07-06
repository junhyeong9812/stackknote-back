package com.stacknote.back.domain.search.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 검색 제안
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "검색 제안")
public class SearchSuggestion {

    @Schema(description = "제안 텍스트")
    private String text;

    @Schema(description = "제안 타입", allowableValues = {"PAGE", "WORKSPACE", "TAG"})
    private String type;

    @Schema(description = "아이콘")
    private String icon;
}