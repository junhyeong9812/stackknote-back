package com.stacknote.back.domain.workspace.dto.response;

import com.stacknote.back.domain.user.dto.response.UserResponse;
import com.stacknote.back.domain.workspace.entity.WorkspaceMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 워크스페이스 멤버 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceMemberResponse {

    private Long id;
    private UserResponse user;
    private String role;
    private Boolean isActive;
    private UserResponse invitedBy;
    private LocalDateTime joinedAt;

    /**
     * WorkspaceMember 엔티티로부터 WorkspaceMemberResponse 생성
     */
    public static WorkspaceMemberResponse from(WorkspaceMember member) {
        return WorkspaceMemberResponse.builder()
                .id(member.getId())
                .user(UserResponse.from(member.getUser()))
                .role(member.getRole().name())
                .isActive(member.getIsActive())
                .invitedBy(member.getInvitedBy() != null ? UserResponse.from(member.getInvitedBy()) : null)
                .joinedAt(member.getCreatedAt())
                .build();
    }
}