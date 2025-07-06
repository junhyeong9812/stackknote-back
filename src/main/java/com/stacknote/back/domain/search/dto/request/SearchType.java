package com.stacknote.back.domain.search.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 검색 타입 Enum
 */
@Schema(description = "검색 타입")
public enum SearchType {

    @Schema(description = "전체 검색")
    ALL,

    @Schema(description = "페이지만 검색")
    PAGE,

    @Schema(description = "워크스페이스만 검색")
    WORKSPACE,

    @Schema(description = "제목만 검색")
    TITLE,

    @Schema(description = "내용만 검색")
    CONTENT,

    @Schema(description = "태그만 검색")
    TAG
}