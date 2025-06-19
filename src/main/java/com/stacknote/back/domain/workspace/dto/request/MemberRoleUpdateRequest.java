package com.stacknote.back.domain.workspace.dto.request;

import com.stacknote.back.domain.workspace.entity.WorkspaceMember;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 멤버 역할 변경 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberRoleUpdateRequest {

    @NotNull(message = "역할은 필수입니다.")
    private WorkspaceMember.Role role;
}