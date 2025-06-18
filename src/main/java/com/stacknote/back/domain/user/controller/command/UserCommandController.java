package com.stacknote.back.domain.user.controller.command;

import com.stacknote.back.domain.user.dto.request.PasswordChangeRequest;
import com.stacknote.back.domain.user.dto.request.UserUpdateRequest;
import com.stacknote.back.domain.user.dto.response.UserResponse;
import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.domain.user.service.command.UserCommandService;
import com.stacknote.back.global.dto.ApiResponse;
import com.stacknote.back.global.utils.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 명령 컨트롤러
 * 사용자 정보 수정, 삭제, 비밀번호 변경 등의 명령 기능 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Commands", description = "사용자 명령 관리 API")
public class UserCommandController {

    private final UserCommandService userCommandService;
    private final CookieUtil cookieUtil;

    /**
     * 사용자 정보 수정
     */
    @PutMapping("/profile")
    @Operation(summary = "사용자 정보 수정", description = "로그인한 사용자의 프로필 정보를 수정합니다.")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        log.info("사용자 정보 수정 요청: {}", currentUser.getId());

        UserResponse userResponse = userCommandService.updateUser(currentUser.getId(), request);

        return ResponseEntity.ok(ApiResponse.success("사용자 정보가 수정되었습니다.", userResponse));
    }

    /**
     * 비밀번호 변경
     */
    @PutMapping("/password")
    @Operation(summary = "비밀번호 변경", description = "로그인한 사용자의 비밀번호를 변경합니다.")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody PasswordChangeRequest request,
            HttpServletResponse response
    ) {
        log.info("비밀번호 변경 요청: {}", currentUser.getId());

        userCommandService.changePassword(currentUser.getId(), request);

        // 비밀번호 변경 시 보안상 모든 쿠키 삭제 (재로그인 필요)
        cookieUtil.deleteAllAuthCookies(response);

        return ResponseEntity.ok(ApiResponse.success("비밀번호가 변경되었습니다. 다시 로그인해주세요."));
    }

    /**
     * 이메일 인증 완료
     */
    @PostMapping("/verify-email")
    @Operation(summary = "이메일 인증", description = "사용자의 이메일 인증을 완료합니다.")
    public ResponseEntity<ApiResponse<UserResponse>> verifyEmail(
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("이메일 인증 요청: {}", currentUser.getId());

        UserResponse userResponse = userCommandService.verifyEmail(currentUser.getId());

        return ResponseEntity.ok(ApiResponse.success("이메일 인증이 완료되었습니다.", userResponse));
    }

    /**
     * 계정 비활성화
     */
    @PostMapping("/deactivate")
    @Operation(summary = "계정 비활성화", description = "사용자 계정을 비활성화합니다.")
    public ResponseEntity<ApiResponse<Void>> deactivateAccount(
            @AuthenticationPrincipal User currentUser,
            HttpServletResponse response
    ) {
        log.info("계정 비활성화 요청: {}", currentUser.getId());

        userCommandService.deactivateAccount(currentUser.getId());

        // 계정 비활성화 시 모든 쿠키 삭제
        cookieUtil.deleteAllAuthCookies(response);

        return ResponseEntity.ok(ApiResponse.success("계정이 비활성화되었습니다."));
    }

    /**
     * 계정 활성화
     */
    @PostMapping("/activate")
    @Operation(summary = "계정 활성화", description = "비활성화된 계정을 다시 활성화합니다.")
    public ResponseEntity<ApiResponse<UserResponse>> activateAccount(
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("계정 활성화 요청: {}", currentUser.getId());

        UserResponse userResponse = userCommandService.activateAccount(currentUser.getId());

        return ResponseEntity.ok(ApiResponse.success("계정이 활성화되었습니다.", userResponse));
    }

    /**
     * 계정 삭제 (소프트 삭제)
     */
    @DeleteMapping("/account")
    @Operation(summary = "계정 삭제", description = "사용자 계정을 삭제합니다. (복구 가능)")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @AuthenticationPrincipal User currentUser,
            HttpServletResponse response
    ) {
        log.info("계정 삭제 요청: {}", currentUser.getId());

        userCommandService.deleteAccount(currentUser.getId());

        // 계정 삭제 시 모든 쿠키 삭제
        cookieUtil.deleteAllAuthCookies(response);

        return ResponseEntity.ok(ApiResponse.success("계정이 삭제되었습니다."));
    }
}