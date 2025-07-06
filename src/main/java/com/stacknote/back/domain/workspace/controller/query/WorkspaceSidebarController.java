package com.stacknote.back.domain.workspace.controller.query;

import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.domain.workspace.dto.response.*;
import com.stacknote.back.domain.workspace.service.query.WorkspaceSidebarService;
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
 * 워크스페이스 사이드바 컨트롤러
 * 노션 스타일의 사이드바를 위한 API 제공
 */
@Slf4j
@RestController
@RequestMapping("/workspaces")
@RequiredArgsConstructor
@Tag(name = "Workspace Sidebar", description = "워크스페이스 사이드바 API")
public class WorkspaceSidebarController {

    private final WorkspaceSidebarService workspaceSidebarService;

    /**
     * 사이드바용 워크스페이스 트리 구조 조회
     * - 사용자가 속한 모든 워크스페이스와 각 워크스페이스의 페이지 트리를 한번에 조회
     * - 개인 페이지와 팀 워크스페이스를 구분하여 반환
     */
    @GetMapping("/sidebar/tree")
    @Operation(summary = "사이드바 전체 트리 조회", description = "사이드바에 표시할 워크스페이스와 페이지 트리 구조를 조회합니다.")
    public ResponseEntity<ApiResponse<WorkspaceSidebarResponse>> getSidebarTree(
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("사이드바 트리 조회 요청: 사용자 {}", currentUser.getId());

        WorkspaceSidebarResponse sidebarData = workspaceSidebarService.getSidebarTree(currentUser);

        return ResponseEntity.ok(ApiResponse.success("사이드바 트리 조회 완료", sidebarData));
    }

    /**
     * 워크스페이스별 페이지 트리 조회 (지연 로딩용)
     * - 특정 워크스페이스의 페이지 트리만 조회
     * - 사이드바에서 워크스페이스를 확장할 때 사용
     */
    @GetMapping("/{workspaceId}/sidebar/pages")
    @Operation(summary = "워크스페이스 페이지 트리", description = "워크스페이스의 페이지 트리를 조회합니다.")
    public ResponseEntity<ApiResponse<List<PageTreeResponse>>> getWorkspacePageTree(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("워크스페이스 페이지 트리 조회 요청: 워크스페이스 {}, 사용자 {}", workspaceId, currentUser.getId());

        List<PageTreeResponse> pageTree = workspaceSidebarService.getWorkspacePageTree(workspaceId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("페이지 트리 조회 완료", pageTree));
    }

    /**
     * 최근 방문한 페이지 목록
     * - 사이드바 상단에 표시할 최근 방문 페이지
     */
    @GetMapping("/sidebar/recent-pages")
    @Operation(summary = "최근 방문 페이지", description = "최근 방문한 페이지 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<RecentPageResponse>>> getRecentPages(
            @Parameter(description = "최대 개수") @RequestParam(defaultValue = "5") int limit,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("최근 방문 페이지 조회 요청: 사용자 {}, 개수 {}", currentUser.getId(), limit);

        List<RecentPageResponse> recentPages = workspaceSidebarService.getRecentPages(currentUser, limit);

        return ResponseEntity.ok(ApiResponse.success("최근 방문 페이지 조회 완료", recentPages));
    }

    /**
     * 즐겨찾기 페이지 목록
     * - 사이드바에 표시할 즐겨찾기 페이지
     */
    @GetMapping("/sidebar/favorite-pages")
    @Operation(summary = "즐겨찾기 페이지", description = "즐겨찾기한 페이지 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<FavoritePageResponse>>> getFavoritePages(
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("즐겨찾기 페이지 조회 요청: 사용자 {}", currentUser.getId());

        List<FavoritePageResponse> favoritePages = workspaceSidebarService.getFavoritePages(currentUser);

        return ResponseEntity.ok(ApiResponse.success("즐겨찾기 페이지 조회 완료", favoritePages));
    }
}