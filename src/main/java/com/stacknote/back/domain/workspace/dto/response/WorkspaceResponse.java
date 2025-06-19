package com.stacknote.back.domain.workspace.dto.response;

import com.stacknote.back.domain.user.dto.response.UserResponse;
import com.stacknote.back.domain.workspace.entity.Workspace;
import com.stacknote.back.domain.workspace.entity.WorkspaceMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 워크스페이스 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceResponse {

    private Long id;
    private String name;
    private String description;
    private String icon;
    private String coverImageUrl;
    private UserResponse owner;
    private String visibility;
    private Boolean isActive;
    private String inviteCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 현재 사용자의 역할 정보
    private String currentUserRole;
    private Boolean canRead;
    private Boolean canWrite;
    private Boolean canManage;

    /**
     * Workspace 엔티티로부터 WorkspaceResponse 생성
     */
    public static WorkspaceResponse from(Workspace workspace) {
        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .icon(workspace.getIcon())
                .coverImageUrl(workspace.getCoverImageUrl())
                .owner(UserResponse.from(workspace.getOwner()))
                .visibility(workspace.getVisibility().name())
                .isActive(workspace.getIsActive())
                .inviteCode(workspace.getInviteCode())
                .createdAt(workspace.getCreatedAt())
                .updatedAt(workspace.getUpdatedAt())
                .build();
    }

    /**
     * 현재 사용자의 권한 정보와 함께 생성
     */
    public static WorkspaceResponse fromWithPermissions(Workspace workspace, WorkspaceMember.Role currentUserRole) {
        WorkspaceResponse response = from(workspace);

        if (currentUserRole != null) {
            response.currentUserRole = currentUserRole.name();
            response.canRead = true; // 모든 멤버는 읽기 가능
            response.canWrite = currentUserRole == WorkspaceMember.Role.OWNER ||
                    currentUserRole == WorkspaceMember.Role.ADMIN ||
                    currentUserRole == WorkspaceMember.Role.MEMBER;
            response.canManage = currentUserRole == WorkspaceMember.Role.OWNER ||
                    currentUserRole == WorkspaceMember.Role.ADMIN;
        }

        return response;
    }
}