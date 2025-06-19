package com.stacknote.back.domain.workspace.controller.query;

import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.domain.workspace.dto.response.WorkspaceMemberResponse;
import com.stacknote.back.domain.workspace.dto.response.WorkspaceResponse;
import com.stacknote.back.domain.workspace.dto.response.WorkspaceSummaryResponse;
import com.stacknote.back.domain.workspace.service.query.WorkspaceQueryService;
import com.stacknote.back.global.dto.ApiResponse;
import com.stacknote.back.global.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 워크스페이스 쿼리 컨트롤러
 * 워크스페이스 조회, 검색 등의 읽기 전용 기능 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
@Tag(name = "Workspace Queries", description = "워크스페이스 조회 관리 API")
public class WorkspaceQueryController {

    private final WorkspaceQueryService workspaceQueryService;

    /**
     * 내 워크스페이스 목록 조회
     */
    @GetMapping("/my")
    @Operation(summary = "내 워크스페이스 목록", description = "현재 사용자가 속한 워크스페이스 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<WorkspaceSummaryResponse>>> getMyWorkspaces(
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("내 워크스페이스 목록 조회 요청: {}", currentUser.getId());

        List<WorkspaceSummaryResponse> workspaces = workspaceQueryService.getUserWorkspaces(currentUser);

        return ResponseEntity.ok(ApiResponse.success("워크스페이스 목록 조회 완료", workspaces));
    }

    /**
     * 워크스페이스 상세 조회
     */
    @GetMapping("/{workspaceId}")
    @Operation(summary = "워크스페이스 상세 조회", description = "특정 워크스페이스의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> getWorkspace(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("워크스페이스 상세 조회 요청: {}, 사용자: {}", workspaceId, currentUser.getId());

        WorkspaceResponse workspace = workspaceQueryService.getWorkspace(workspaceId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("워크스페이스 조회 완료", workspace));
    }

    /**
     * 워크스페이스 멤버 목록 조회
     */
    @GetMapping("/{workspaceId}/members")
    @Operation(summary = "워크스페이스 멤버 목록", description = "워크스페이스의 멤버 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<WorkspaceMemberResponse>>> getWorkspaceMembers(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("워크스페이스 멤버 목록 조회 요청: {}, 사용자: {}", workspaceId, currentUser.getId());

        List<WorkspaceMemberResponse> members = workspaceQueryService.getWorkspaceMembers(workspaceId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("멤버 목록 조회 완료", members));
    }

    /**
     * 워크스페이스 멤버 목록 조회 (페이징)
     */
    @GetMapping("/{workspaceId}/members/paged")
    @Operation(summary = "워크스페이스 멤버 목록 (페이징)", description = "워크스페이스의 멤버 목록을 페이징하여 조회합니다.")
    public ResponseEntity<ApiResponse<PageResponse<WorkspaceMemberResponse>>> getWorkspaceMembersPaged(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "createdAt") String sort,
            @Parameter(description = "정렬 방향") @RequestParam(defaultValue = "asc") String direction
    ) {
        log.debug("워크스페이스 멤버 목록 조회 (페이징): {}, 사용자: {}", workspaceId, currentUser.getId());

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PageResponse<WorkspaceMemberResponse> memberPage = workspaceQueryService.getWorkspaceMembers(
                workspaceId, currentUser, pageable);

        return ResponseEntity.ok(ApiResponse.success("멤버 목록 조회 완료", memberPage));
    }

    /**
     * 워크스페이스 검색
     */
    @GetMapping("/search")
    @Operation(summary = "워크스페이스 검색", description = "이름으로 워크스페이스를 검색합니다.")
    public ResponseEntity<ApiResponse<List<WorkspaceSummaryResponse>>> searchWorkspaces(
            @Parameter(description = "검색 키워드") @RequestParam String keyword,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("워크스페이스 검색 요청: 키워드: {}, 사용자: {}", keyword, currentUser.getId());

        List<WorkspaceSummaryResponse> workspaces = workspaceQueryService.searchWorkspaces(currentUser, keyword);

        return ResponseEntity.ok(ApiResponse.success("워크스페이스 검색 완료", workspaces));
    }

    /**
     * 공개 워크스페이스 목록 조회
     */
    @GetMapping("/public")
    @Operation(summary = "공개 워크스페이스 목록", description = "공개된 워크스페이스 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<PageResponse<WorkspaceResponse>>> getPublicWorkspaces(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "createdAt") String sort,
            @Parameter(description = "정렬 방향") @RequestParam(defaultValue = "desc") String direction
    ) {
        log.debug("공개 워크스페이스 목록 조회 요청");

        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PageResponse<WorkspaceResponse> workspacePage = workspaceQueryService.getPublicWorkspaces(pageable);

        return ResponseEntity.ok(ApiResponse.success("공개 워크스페이스 목록 조회 완료", workspacePage));
    }

    /**
     * 사용자 워크스페이스 통계 조회
     */
    @GetMapping("/my/statistics")
    @Operation(summary = "내 워크스페이스 통계", description = "현재 사용자의 워크스페이스 통계를 조회합니다.")
    public ResponseEntity<ApiResponse<WorkspaceQueryService.WorkspaceStatisticsResponse>> getUserWorkspaceStatistics(
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("사용자 워크스페이스 통계 조회 요청: {}", currentUser.getId());

        WorkspaceQueryService.WorkspaceStatisticsResponse statistics =
                workspaceQueryService.getUserWorkspaceStatistics(currentUser);

        return ResponseEntity.ok(ApiResponse.success("워크스페이스 통계 조회 완료", statistics));
    }

    /**
     * 초대 코드로 워크스페이스 정보 미리보기
     */
    @GetMapping("/preview/{inviteCode}")
    @Operation(summary = "초대 코드 미리보기", description = "초대 코드로 워크스페이스 정보를 미리 확인합니다.")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> previewWorkspaceByInviteCode(
            @Parameter(description = "초대 코드") @PathVariable String inviteCode
    ) {
        log.debug("초대 코드 미리보기 요청: {}", inviteCode);

        WorkspaceResponse workspace = workspaceQueryService.getWorkspaceByInviteCode(inviteCode);

        return ResponseEntity.ok(ApiResponse.success("워크스페이스 미리보기 완료", workspace));
    }
}