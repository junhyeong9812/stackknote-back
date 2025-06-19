package com.stacknote.back.domain.workspace.dto.request;

import com.stacknote.back.domain.workspace.entity.WorkspaceMember;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 멤버 초대 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberInviteRequest {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "유효한 이메일 형식이어야 합니다.")
    private String email;

    @NotNull(message = "역할은 필수입니다.")
    private WorkspaceMember.Role role;

    private String message; // 초대 메시지 (옵션)
}