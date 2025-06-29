package com.stacknote.back.domain.page.controller.query;

import com.stacknote.back.domain.page.dto.response.PageResponse;
import com.stacknote.back.domain.page.dto.response.PageSummaryResponse;
import com.stacknote.back.domain.page.entity.Page;
import com.stacknote.back.domain.page.service.query.PageQueryService;
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
 * 페이지 쿼리 컨트롤러
 * 페이지 조회, 검색 등의 읽기 전용 기능 제공
 */
@Slf4j
@RestController
@RequestMapping("/workspaces/{workspaceId}/pages")
@RequiredArgsConstructor
@Tag(name = "Page Queries", description = "페이지 조회 관리 API")
public class PageQueryController {

    private final PageQueryService pageQueryService;

    /**
     * 워크스페이스의 모든 페이지 목록 조회
     */
    @GetMapping
    @Operation(summary = "워크스페이스 페이지 목록", description = "워크스페이스의 모든 페이지 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<PageSummaryResponse>>> getWorkspacePages(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("워크스페이스 페이지 목록 조회 요청: {}, 사용자: {}", workspaceId, currentUser.getId());

        List<PageSummaryResponse> pages = pageQueryService.getWorkspacePages(workspaceId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("페이지 목록 조회 완료", pages));
    }

    /**
     * 워크스페이스의 최상위 페이지들 조회
     */
    @GetMapping("/root")
    @Operation(summary = "최상위 페이지 목록", description = "워크스페이스의 최상위 페이지들을 조회합니다.")
    public ResponseEntity<ApiResponse<List<PageSummaryResponse>>> getRootPages(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("최상위 페이지 목록 조회 요청: {}, 사용자: {}", workspaceId, currentUser.getId());

        List<PageSummaryResponse> pages = pageQueryService.getRootPages(workspaceId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("최상위 페이지 목록 조회 완료", pages));
    }

    /**
     * 페이지 상세 조회
     */
    @GetMapping("/{pageId}")
    @Operation(summary = "페이지 상세 조회", description = "특정 페이지의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<PageResponse>> getPage(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "페이지 ID") @PathVariable Long pageId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("페이지 상세 조회 요청: {}, 사용자: {}", pageId, currentUser.getId());

        PageResponse page = pageQueryService.getPage(pageId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("페이지 조회 완료", page));
    }

    /**
     * 특정 페이지의 자식 페이지들 조회
     */
    @GetMapping("/{pageId}/children")
    @Operation(summary = "자식 페이지 목록", description = "특정 페이지의 자식 페이지들을 조회합니다.")
    public ResponseEntity<ApiResponse<List<PageSummaryResponse>>> getChildPages(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "페이지 ID") @PathVariable Long pageId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("자식 페이지 목록 조회 요청: {}, 사용자: {}", pageId, currentUser.getId());

        List<PageSummaryResponse> pages = pageQueryService.getChildPages(pageId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("자식 페이지 목록 조회 완료", pages));
    }

    /**
     * 워크스페이스 내 페이지 검색
     */
    @GetMapping("/search")
    @Operation(summary = "페이지 검색", description = "제목이나 내용으로 페이지를 검색합니다.")
    public ResponseEntity<ApiResponse<List<PageSummaryResponse>>> searchPages(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "검색 키워드") @RequestParam String keyword,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("페이지 검색 요청: 워크스페이스: {}, 키워드: {}, 사용자: {}", workspaceId, keyword, currentUser.getId());

        List<PageSummaryResponse> pages = pageQueryService.searchPages(workspaceId, keyword, currentUser);

        return ResponseEntity.ok(ApiResponse.success("페이지 검색 완료", pages));
    }

    /**
     * 최근 수정된 페이지 목록 조회
     */
    @GetMapping("/recent")
    @Operation(summary = "최근 수정된 페이지", description = "최근 수정된 페이지 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<PageSummaryResponse>>> getRecentlyModifiedPages(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "조회할 일수") @RequestParam(defaultValue = "7") int days,
            @Parameter(description = "최대 개수") @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("최근 수정된 페이지 목록 조회 요청: 워크스페이스: {}, 사용자: {}", workspaceId, currentUser.getId());

        List<PageSummaryResponse> pages = pageQueryService.getRecentlyModifiedPages(workspaceId, currentUser, days, limit);

        return ResponseEntity.ok(ApiResponse.success("최근 수정된 페이지 목록 조회 완료", pages));
    }

    /**
     * 공개된 페이지 목록 조회
     */
    @GetMapping("/published")
    @Operation(summary = "공개된 페이지 목록", description = "공개된 페이지 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<PageSummaryResponse>>> getPublishedPages(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("공개된 페이지 목록 조회 요청: 워크스페이스: {}, 사용자: {}", workspaceId, currentUser.getId());

        List<PageSummaryResponse> pages = pageQueryService.getPublishedPages(workspaceId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("공개된 페이지 목록 조회 완료", pages));
    }

    /**
     * 템플릿 페이지 목록 조회
     */
    @GetMapping("/templates")
    @Operation(summary = "템플릿 페이지 목록", description = "템플릿 페이지 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<PageSummaryResponse>>> getTemplatePages(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("템플릿 페이지 목록 조회 요청: 워크스페이스: {}, 사용자: {}", workspaceId, currentUser.getId());

        List<PageSummaryResponse> pages = pageQueryService.getTemplatePages(workspaceId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("템플릿 페이지 목록 조회 완료", pages));
    }

    /**
     * 페이지 타입별 조회
     */
    @GetMapping("/type/{pageType}")
    @Operation(summary = "페이지 타입별 조회", description = "특정 타입의 페이지들을 조회합니다.")
    public ResponseEntity<ApiResponse<List<PageSummaryResponse>>> getPagesByType(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "페이지 타입") @PathVariable Page.PageType pageType,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("페이지 타입별 조회 요청: 워크스페이스: {}, 타입: {}, 사용자: {}", workspaceId, pageType, currentUser.getId());

        List<PageSummaryResponse> pages = pageQueryService.getPagesByType(workspaceId, pageType, currentUser);

        return ResponseEntity.ok(ApiResponse.success("페이지 타입별 조회 완료", pages));
    }

    /**
     * 인기 페이지 목록 조회
     */
    @GetMapping("/popular")
    @Operation(summary = "인기 페이지 목록", description = "조회수가 높은 인기 페이지들을 조회합니다.")
    public ResponseEntity<ApiResponse<List<PageSummaryResponse>>> getPopularPages(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "최대 개수") @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("인기 페이지 목록 조회 요청: 워크스페이스: {}, 사용자: {}", workspaceId, currentUser.getId());

        List<PageSummaryResponse> pages = pageQueryService.getPopularPages(workspaceId, currentUser, limit);

        return ResponseEntity.ok(ApiResponse.success("인기 페이지 목록 조회 완료", pages));
    }

    /**
     * 페이지 히스토리 조회
     */
    @GetMapping("/{pageId}/history")
    @Operation(summary = "페이지 히스토리", description = "페이지의 변경 이력을 조회합니다.")
    public ResponseEntity<ApiResponse<List<PageQueryService.PageHistoryResponse>>> getPageHistory(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "페이지 ID") @PathVariable Long pageId,
            @Parameter(description = "최대 개수") @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("페이지 히스토리 조회 요청: {}, 사용자: {}", pageId, currentUser.getId());

        List<PageQueryService.PageHistoryResponse> history = pageQueryService.getPageHistory(pageId, currentUser, limit);

        return ResponseEntity.ok(ApiResponse.success("페이지 히스토리 조회 완료", history));
    }

    /**
     * 특정 버전의 페이지 히스토리 조회
     */
    @GetMapping("/{pageId}/history/{version}")
    @Operation(summary = "페이지 히스토리 버전 조회", description = "특정 버전의 페이지 히스토리를 조회합니다.")
    public ResponseEntity<ApiResponse<PageQueryService.PageHistoryResponse>> getPageHistoryVersion(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "페이지 ID") @PathVariable Long pageId,
            @Parameter(description = "버전 번호") @PathVariable Integer version,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("페이지 히스토리 버전 조회 요청: {}, 버전: {}, 사용자: {}", pageId, version, currentUser.getId());

        PageQueryService.PageHistoryResponse history = pageQueryService.getPageHistoryVersion(pageId, version, currentUser);

        return ResponseEntity.ok(ApiResponse.success("페이지 히스토리 버전 조회 완료", history));
    }

    /**
     * 워크스페이스 페이지 통계 조회
     */
    @GetMapping("/statistics")
    @Operation(summary = "페이지 통계", description = "워크스페이스의 페이지 통계를 조회합니다.")
    public ResponseEntity<ApiResponse<PageQueryService.PageStatisticsResponse>> getPageStatistics(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("페이지 통계 조회 요청: 워크스페이스: {}, 사용자: {}", workspaceId, currentUser.getId());

        PageQueryService.PageStatisticsResponse statistics = pageQueryService.getWorkspacePageStatistics(workspaceId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("페이지 통계 조회 완료", statistics));
    }
}