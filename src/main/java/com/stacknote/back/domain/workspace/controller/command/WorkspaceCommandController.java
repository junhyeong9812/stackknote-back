package com.stacknote.back.domain.workspace.controller.command;

import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.domain.workspace.dto.request.MemberInviteRequest;
import com.stacknote.back.domain.workspace.dto.request.MemberRoleUpdateRequest;
import com.stacknote.back.domain.workspace.dto.request.WorkspaceCreateRequest;
import com.stacknote.back.domain.workspace.dto.request.WorkspaceUpdateRequest;
import com.stacknote.back.domain.workspace.dto.response.WorkspaceMemberResponse;
import com.stacknote.back.domain.workspace.dto.response.WorkspaceResponse;
import com.stacknote.back.domain.workspace.service.command.WorkspaceCommandService;
import com.stacknote.back.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 워크스페이스 명령 컨트롤러
 * 워크스페이스 생성, 수정, 삭제, 멤버 관리 등의 명령 기능 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
@Tag(name = "Workspace Commands", description = "워크스페이스 명령 관리 API")
public class WorkspaceCommandController {

    private final WorkspaceCommandService workspaceCommandService;

    /**
     * 워크스페이스 생성
     */
    @PostMapping
    @Operation(summary = "워크스페이스 생성", description = "새로운 워크스페이스를 생성합니다.")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> createWorkspace(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody WorkspaceCreateRequest request
    ) {
        log.info("워크스페이스 생성 요청: {}, 사용자: {}", request.getName(), currentUser.getId());

        WorkspaceResponse response = workspaceCommandService.createWorkspace(currentUser, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("워크스페이스가 생성되었습니다.", response));
    }

    /**
     * 워크스페이스 정보 수정
     */
    @PutMapping("/{workspaceId}")
    @Operation(summary = "워크스페이스 수정", description = "워크스페이스 정보를 수정합니다.")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> updateWorkspace(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody WorkspaceUpdateRequest request
    ) {
        log.info("워크스페이스 수정 요청: {}, 사용자: {}", workspaceId, currentUser.getId());

        WorkspaceResponse response = workspaceCommandService.updateWorkspace(workspaceId, currentUser, request);

        return ResponseEntity.ok(ApiResponse.success("워크스페이스가 수정되었습니다.", response));
    }

    /**
     * 워크스페이스 삭제
     */
    @DeleteMapping("/{workspaceId}")
    @Operation(summary = "워크스페이스 삭제", description = "워크스페이스를 삭제합니다. (소유자만 가능)")
    public ResponseEntity<ApiResponse<Void>> deleteWorkspace(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("워크스페이스 삭제 요청: {}, 사용자: {}", workspaceId, currentUser.getId());

        workspaceCommandService.deleteWorkspace(workspaceId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("워크스페이스가 삭제되었습니다."));
    }

    /**
     * 멤버 초대
     */
    @PostMapping("/{workspaceId}/members")
    @Operation(summary = "멤버 초대", description = "워크스페이스에 새로운 멤버를 초대합니다.")
    public ResponseEntity<ApiResponse<WorkspaceMemberResponse>> inviteMember(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody MemberInviteRequest request
    ) {
        log.info("멤버 초대 요청: {}, 초대자: {}, 이메일: {}", workspaceId, currentUser.getId(), request.getEmail());

        WorkspaceMemberResponse response = workspaceCommandService.inviteMember(workspaceId, currentUser, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("멤버가 초대되었습니다.", response));
    }

    /**
     * 멤버 역할 변경
     */
    @PutMapping("/{workspaceId}/members/{memberId}/role")
    @Operation(summary = "멤버 역할 변경", description = "워크스페이스 멤버의 역할을 변경합니다.")
    public ResponseEntity<ApiResponse<WorkspaceMemberResponse>> updateMemberRole(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "멤버 ID") @PathVariable Long memberId,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody MemberRoleUpdateRequest request
    ) {
        log.info("멤버 역할 변경 요청: {}, 멤버ID: {}, 사용자: {}", workspaceId, memberId, currentUser.getId());

        WorkspaceMemberResponse response = workspaceCommandService.updateMemberRole(
                workspaceId, memberId, currentUser, request);

        return ResponseEntity.ok(ApiResponse.success("멤버 역할이 변경되었습니다.", response));
    }

    /**
     * 멤버 제거
     */
    @DeleteMapping("/{workspaceId}/members/{memberId}")
    @Operation(summary = "멤버 제거", description = "워크스페이스에서 멤버를 제거합니다.")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "멤버 ID") @PathVariable Long memberId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("멤버 제거 요청: {}, 멤버ID: {}, 사용자: {}", workspaceId, memberId, currentUser.getId());

        workspaceCommandService.removeMember(workspaceId, memberId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("멤버가 제거되었습니다."));
    }

    /**
     * 워크스페이스 나가기
     */
    @PostMapping("/{workspaceId}/leave")
    @Operation(summary = "워크스페이스 나가기", description = "현재 사용자가 워크스페이스를 나갑니다.")
    public ResponseEntity<ApiResponse<Void>> leaveWorkspace(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("워크스페이스 나가기 요청: {}, 사용자: {}", workspaceId, currentUser.getId());

        workspaceCommandService.leaveWorkspace(workspaceId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("워크스페이스를 나갔습니다."));
    }

    /**
     * 초대 코드 생성
     */
    @PostMapping("/{workspaceId}/invite-code")
    @Operation(summary = "초대 코드 생성", description = "워크스페이스 초대 코드를 생성합니다.")
    public ResponseEntity<ApiResponse<String>> generateInviteCode(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("초대 코드 생성 요청: {}, 사용자: {}", workspaceId, currentUser.getId());

        String inviteCode = workspaceCommandService.generateInviteCode(workspaceId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("초대 코드가 생성되었습니다.", inviteCode));
    }

    /**
     * 초대 코드 제거
     */
    @DeleteMapping("/{workspaceId}/invite-code")
    @Operation(summary = "초대 코드 제거", description = "워크스페이스 초대 코드를 제거합니다.")
    public ResponseEntity<ApiResponse<Void>> removeInviteCode(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("초대 코드 제거 요청: {}, 사용자: {}", workspaceId, currentUser.getId());

        workspaceCommandService.removeInviteCode(workspaceId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("초대 코드가 제거되었습니다."));
    }

    /**
     * 초대 코드로 워크스페이스 참가
     */
    @PostMapping("/join/{inviteCode}")
    @Operation(summary = "초대 코드로 참가", description = "초대 코드를 사용하여 워크스페이스에 참가합니다.")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> joinWorkspaceByInviteCode(
            @Parameter(description = "초대 코드") @PathVariable String inviteCode,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("초대 코드로 워크스페이스 참가 요청: {}, 사용자: {}", inviteCode, currentUser.getId());

        WorkspaceResponse response = workspaceCommandService.joinWorkspaceByInviteCode(inviteCode, currentUser);

        return ResponseEntity.ok(ApiResponse.success("워크스페이스에 참가했습니다.", response));
    }
}