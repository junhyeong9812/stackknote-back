package com.stacknote.back.domain.user.controller.query;

import com.stacknote.back.domain.user.dto.response.UserProfileResponse;
import com.stacknote.back.domain.user.dto.response.UserResponse;
import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.domain.user.service.query.UserQueryService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 쿼리 컨트롤러
 * 사용자 조회, 검색 등의 읽기 전용 기능 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Queries", description = "사용자 조회 관리 API")
public class UserQueryController {

    private final UserQueryService userQueryService;

    /**
     * 내 프로필 조회
     */
    @GetMapping("/me")
    @Operation(summary = "내 프로필 조회", description = "로그인한 사용자의 상세 프로필 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("내 프로필 조회 요청: {}", currentUser.getId());

        UserProfileResponse profileResponse = userQueryService.getUserProfile(currentUser.getId());

        return ResponseEntity.ok(ApiResponse.success("프로필 조회 완료", profileResponse));
    }

    /**
     * 사용자 기본 정보 조회
     */
    @GetMapping("/{userId}")
    @Operation(summary = "사용자 정보 조회", description = "특정 사용자의 기본 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @Parameter(description = "사용자 ID") @PathVariable Long userId
    ) {
        log.debug("사용자 정보 조회 요청: {}", userId);

        UserResponse userResponse = userQueryService.getUserById(userId);

        return ResponseEntity.ok(ApiResponse.success("사용자 정보 조회 완료", userResponse));
    }

    /**
     * 사용자명으로 사용자 조회
     */
    @GetMapping("/username/{username}")
    @Operation(summary = "사용자명으로 조회", description = "사용자명으로 사용자 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByUsername(
            @Parameter(description = "사용자명") @PathVariable String username
    ) {
        log.debug("사용자명으로 조회 요청: {}", username);

        UserResponse userResponse = userQueryService.getUserByUsername(username);

        return ResponseEntity.ok(ApiResponse.success("사용자 조회 완료", userResponse));
    }

    /**
     * 이메일 중복 확인
     */
    @GetMapping("/check-email")
    @Operation(summary = "이메일 중복 확인", description = "이메일 사용 가능 여부를 확인합니다.")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailAvailability(
            @Parameter(description = "확인할 이메일") @RequestParam String email
    ) {
        log.debug("이메일 중복 확인 요청: {}", email);

        boolean isAvailable = !userQueryService.isEmailExists(email);
        String message = isAvailable ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다.";

        return ResponseEntity.ok(ApiResponse.success(message, isAvailable));
    }

    /**
     * 사용자명 중복 확인
     */
    @GetMapping("/check-username")
    @Operation(summary = "사용자명 중복 확인", description = "사용자명 사용 가능 여부를 확인합니다.")
    public ResponseEntity<ApiResponse<Boolean>> checkUsernameAvailability(
            @Parameter(description = "확인할 사용자명") @RequestParam String username
    ) {
        log.debug("사용자명 중복 확인 요청: {}", username);

        boolean isAvailable = !userQueryService.isUsernameExists(username);
        String message = isAvailable ? "사용 가능한 사용자명입니다." : "이미 사용 중인 사용자명입니다.";

        return ResponseEntity.ok(ApiResponse.success(message, isAvailable));
    }

    /**
     * 전체 사용자 목록 조회 (관리자 전용)
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "전체 사용자 목록", description = "모든 사용자 목록을 페이징하여 조회합니다. (관리자 전용)")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "createdAt") String sort,
            @Parameter(description = "정렬 방향") @RequestParam(defaultValue = "desc") String direction
    ) {
        log.debug("전체 사용자 목록 조회 요청 - 페이지: {}, 크기: {}", page, size);

        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PageResponse<UserResponse> pageResponse = userQueryService.getAllUsers(pageable);

        return ResponseEntity.ok(ApiResponse.success("사용자 목록 조회 완료", pageResponse));
    }

    /**
     * 활성 사용자 수 조회 (관리자 전용)
     */
    @GetMapping("/admin/count")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "활성 사용자 수", description = "활성 상태인 사용자의 총 수를 조회합니다. (관리자 전용)")
    public ResponseEntity<ApiResponse<Long>> getActiveUserCount() {
        log.debug("활성 사용자 수 조회 요청");

        long activeUserCount = userQueryService.getActiveUserCount();

        return ResponseEntity.ok(ApiResponse.success("활성 사용자 수 조회 완료", activeUserCount));
    }
}