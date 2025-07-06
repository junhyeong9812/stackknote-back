package com.stacknote.back.domain.search.controller;

import com.stacknote.back.domain.search.dto.request.SearchType;
import com.stacknote.back.domain.search.dto.response.GlobalSearchResponse;
import com.stacknote.back.domain.search.dto.response.SearchSuggestion;
import com.stacknote.back.domain.search.service.GlobalSearchService;
import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 전역 검색 컨트롤러
 * 워크스페이스와 페이지를 통합하여 검색하는 기능 제공
 */
@Slf4j
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Tag(name = "Global Search", description = "전역 검색 API")
public class GlobalSearchController {

    private final GlobalSearchService globalSearchService;

    /**
     * 전역 검색
     * - 모든 워크스페이스와 페이지를 대상으로 검색
     * - 검색 결과를 워크스페이스별로 그룹화하여 반환
     */
    @GetMapping
    @Operation(summary = "전역 검색", description = "모든 워크스페이스와 페이지를 검색합니다.")
    public ResponseEntity<ApiResponse<GlobalSearchResponse>> search(
            @Parameter(description = "검색어") @RequestParam String query,
            @Parameter(description = "워크스페이스 ID (특정 워크스페이스만 검색)") @RequestParam(required = false) Long workspaceId,
            @Parameter(description = "검색 타입") @RequestParam(defaultValue = "ALL") SearchType type,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("전역 검색 요청: 검색어 {}, 워크스페이스 {}, 타입 {}, 사용자 {}",
                query, workspaceId, type, currentUser.getId());

        GlobalSearchResponse searchResult = globalSearchService.search(
                query, workspaceId, type, currentUser
        );

        return ResponseEntity.ok(ApiResponse.success("검색 완료", searchResult));
    }

    /**
     * 검색 제안
     * - 입력한 검색어에 대한 자동완성 제안
     */
    @GetMapping("/suggestions")
    @Operation(summary = "검색 제안", description = "검색어 자동완성 제안을 제공합니다.")
    public ResponseEntity<ApiResponse<List<SearchSuggestion>>> getSearchSuggestions(
            @Parameter(description = "검색어") @RequestParam String query,
            @Parameter(description = "최대 개수") @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("검색 제안 요청: 검색어 {}, 개수 {}, 사용자 {}", query, limit, currentUser.getId());

        List<SearchSuggestion> suggestions = globalSearchService.getSuggestions(
                query, limit, currentUser
        );

        return ResponseEntity.ok(ApiResponse.success("검색 제안 조회 완료", suggestions));
    }
}