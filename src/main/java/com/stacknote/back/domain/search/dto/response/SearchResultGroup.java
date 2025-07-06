package com.stacknote.back.domain.search.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 검색 결과 그룹 (워크스페이스별 그룹화)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "검색 결과 그룹")
public class SearchResultGroup {

    @Schema(description = "워크스페이스 ID")
    private Long workspaceId;

    @Schema(description = "워크스페이스 이름")
    private String workspaceName;

    @Schema(description = "검색 결과 항목 목록")
    @Builder.Default
    private List<SearchResultItem> items = new ArrayList<>();
}