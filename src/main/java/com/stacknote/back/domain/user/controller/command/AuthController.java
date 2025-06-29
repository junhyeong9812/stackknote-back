package com.stacknote.back.domain.user.controller.command;

import com.stacknote.back.domain.user.dto.request.UserRegisterRequest;
import com.stacknote.back.domain.user.dto.response.UserResponse;
import com.stacknote.back.domain.user.service.command.AuthCommandService;
import com.stacknote.back.global.dto.ApiResponse;
import com.stacknote.back.global.utils.CookieUtil;
import com.stacknote.back.global.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 컨트롤러 (Filter 적용 후 리팩토링)
 * - 로그인, 토큰갱신: Filter에서 처리
 * - 회원가입, 로그아웃, 상태확인: Controller에서 처리
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "인증 관리 API")
public class AuthController {

    private final AuthCommandService authCommandService;
    private final CookieUtil cookieUtil;
    private final JwtUtil jwtUtil;

    /**
     * 회원가입 (Controller에서 계속 처리)
     */
    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "새로운 사용자 계정을 생성합니다.")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody UserRegisterRequest request
    ) {
        log.info("회원가입 요청: {}", request.getEmail());

        UserResponse userResponse = authCommandService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입이 완료되었습니다.", userResponse));
    }

    /**
     * 로그인 - Filter에서 처리되므로 이 메서드는 호출되지 않음
     * Swagger 문서화를 위해 유지
     */
    @PostMapping("/login")
    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호로 로그인합니다. (Filter에서 처리됨)"
    )
    public ResponseEntity<ApiResponse<String>> login() {
        // 이 메서드는 실제로 실행되지 않음
        // CustomLoginFilter에서 먼저 처리되어 응답이 반환됨
        return ResponseEntity.ok(ApiResponse.success("Filter에서 처리됨"));
    }

    /**
     * 토큰 갱신 - Filter에서 처리되므로 이 메서드는 호출되지 않음
     * Swagger 문서화를 위해 유지
     */
    @PostMapping("/refresh")
    @Operation(
            summary = "토큰 갱신",
            description = "리프레시 토큰으로 새로운 액세스 토큰을 발급받습니다. (Filter에서 처리됨)"
    )
    public ResponseEntity<ApiResponse<String>> refreshToken() {
        // 이 메서드는 실제로 실행되지 않음
        // TokenReissueFilter에서 먼저 처리되어 응답이 반환됨
        return ResponseEntity.ok(ApiResponse.success("Filter에서 처리됨"));
    }

    /**
     * 로그아웃 (Controller에서 계속 처리)
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "현재 세션에서 로그아웃합니다.")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        log.info("로그아웃 요청");

        String accessToken = cookieUtil.getAccessToken(httpRequest)
                .orElse(null);

        if (accessToken != null) {
            authCommandService.logout(accessToken);
        }

        // 모든 인증 쿠키 삭제
        cookieUtil.deleteAllAuthCookies(httpResponse);

        return ResponseEntity.ok(ApiResponse.success("로그아웃이 완료되었습니다."));
    }

    /**
     * 모든 디바이스에서 로그아웃 (Controller에서 계속 처리)
     */
    @PostMapping("/logout-all")
    @Operation(summary = "전체 로그아웃", description = "모든 디바이스에서 로그아웃합니다.")
    public ResponseEntity<ApiResponse<Void>> logoutFromAllDevices(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        log.info("전체 로그아웃 요청");

        String accessToken = cookieUtil.getAccessToken(httpRequest)
                .orElseThrow(() -> new IllegalArgumentException("액세스 토큰이 없습니다."));

        // JWT에서 사용자 ID 추출하여 모든 디바이스 로그아웃
        Long userId = jwtUtil.getUserIdFromToken(accessToken);
        authCommandService.logoutFromAllDevices(userId);

        // 현재 세션의 쿠키 삭제
        cookieUtil.deleteAllAuthCookies(httpResponse);

        return ResponseEntity.ok(ApiResponse.success("모든 디바이스에서 로그아웃되었습니다."));
    }

    /**
     * 인증 상태 확인 (Controller에서 계속 처리)
     */
    @GetMapping("/status")
    @Operation(summary = "인증 상태 확인", description = "현재 로그인 상태를 확인합니다.")
    public ResponseEntity<ApiResponse<Boolean>> checkAuthStatus(HttpServletRequest request) {
        boolean hasAuthCookies = cookieUtil.hasAuthCookies(request);
        return ResponseEntity.ok(ApiResponse.success("인증 상태 조회 완료", hasAuthCookies));
    }
}
//package com.stacknote.back.domain.user.controller.command;
//
//import com.stacknote.back.domain.user.dto.request.UserLoginRequest;
//import com.stacknote.back.domain.user.dto.request.UserRegisterRequest;
//import com.stacknote.back.domain.user.dto.response.AuthTokenResponse;
//import com.stacknote.back.domain.user.dto.response.UserResponse;
//import com.stacknote.back.domain.user.service.command.AuthCommandService;
//import com.stacknote.back.global.dto.ApiResponse;
//import com.stacknote.back.global.utils.CookieUtil;
//import com.stacknote.back.global.utils.JwtUtil;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
///**
// * 인증 관련 컨트롤러
// * 회원가입, 로그인, 로그아웃, 토큰 갱신 등의 인증 기능 제공
// */
//@Slf4j
//@RestController
//@RequestMapping("/api/auth")
//@RequiredArgsConstructor
//@Tag(name = "Authentication", description = "인증 관리 API")
//public class AuthController {
//
//    private final AuthCommandService authCommandService;
//    private final CookieUtil cookieUtil;
//    private final JwtUtil jwtUtil;
//
//    /**
//     * 회원가입
//     */
//    @PostMapping("/register")
//    @Operation(summary = "회원가입", description = "새로운 사용자 계정을 생성합니다.")
//    public ResponseEntity<ApiResponse<UserResponse>> register(
//            @Valid @RequestBody UserRegisterRequest request
//    ) {
//        log.info("회원가입 요청: {}", request.getEmail());
//
//        UserResponse userResponse = authCommandService.register(request);
//
//        return ResponseEntity
//                .status(HttpStatus.CREATED)
//                .body(ApiResponse.success("회원가입이 완료되었습니다.", userResponse));
//    }
//
//    /**
//     * 로그인
//     */
//    @PostMapping("/login")
//    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
//    public ResponseEntity<ApiResponse<AuthTokenResponse>> login(
//            @Valid @RequestBody UserLoginRequest request,
//            HttpServletRequest httpRequest,
//            HttpServletResponse httpResponse
//    ) {
//        log.info("로그인 요청: {}", request.getEmail());
//
//        // User-Agent와 IP 주소 추출
//        String userAgent = httpRequest.getHeader("User-Agent");
//        String ipAddress = getClientIpAddress(httpRequest);
//
//        AuthTokenResponse authResponse = authCommandService.login(request, userAgent, ipAddress);
//
//        // 토큰을 쿠키에 저장
//        setAuthCookies(httpResponse, authResponse);
//
//        return ResponseEntity.ok(ApiResponse.success("로그인이 완료되었습니다.", authResponse));
//    }
//
//    /**
//     * 토큰 갱신
//     */
//    @PostMapping("/refresh")
//    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 새로운 액세스 토큰을 발급받습니다.")
//    public ResponseEntity<ApiResponse<AuthTokenResponse>> refreshToken(
//            HttpServletRequest httpRequest,
//            HttpServletResponse httpResponse
//    ) {
//        log.info("토큰 갱신 요청");
//
//        String refreshToken = cookieUtil.getRefreshToken(httpRequest)
//                .orElseThrow(() -> new IllegalArgumentException("리프레시 토큰이 없습니다."));
//
//        String userAgent = httpRequest.getHeader("User-Agent");
//        String ipAddress = getClientIpAddress(httpRequest);
//
//        AuthTokenResponse authResponse = authCommandService.refreshToken(refreshToken, userAgent, ipAddress);
//
//        // 새로운 액세스 토큰을 쿠키에 저장
//        cookieUtil.setAccessTokenCookie(httpResponse, authResponse.getAccessToken());
//
//        return ResponseEntity.ok(ApiResponse.success("토큰이 갱신되었습니다.", authResponse));
//    }
//
//    /**
//     * 로그아웃
//     */
//    @PostMapping("/logout")
//    @Operation(summary = "로그아웃", description = "현재 세션에서 로그아웃합니다.")
//    public ResponseEntity<ApiResponse<Void>> logout(
//            HttpServletRequest httpRequest,
//            HttpServletResponse httpResponse
//    ) {
//        log.info("로그아웃 요청");
//
//        String accessToken = cookieUtil.getAccessToken(httpRequest)
//                .orElse(null);
//
//        if (accessToken != null) {
//            authCommandService.logout(accessToken);
//        }
//
//        // 모든 인증 쿠키 삭제
//        cookieUtil.deleteAllAuthCookies(httpResponse);
//
//        return ResponseEntity.ok(ApiResponse.success("로그아웃이 완료되었습니다."));
//    }
//
//    /**
//     * 모든 디바이스에서 로그아웃
//     */
//    @PostMapping("/logout-all")
//    @Operation(summary = "전체 로그아웃", description = "모든 디바이스에서 로그아웃합니다.")
//    public ResponseEntity<ApiResponse<Void>> logoutFromAllDevices(
//            HttpServletRequest httpRequest,
//            HttpServletResponse httpResponse
//    ) {
//        log.info("전체 로그아웃 요청");
//
//        String accessToken = cookieUtil.getAccessToken(httpRequest)
//                .orElseThrow(() -> new IllegalArgumentException("액세스 토큰이 없습니다."));
//
//        // JWT에서 사용자 ID 추출하여 모든 디바이스 로그아웃
//        Long userId = jwtUtil.getUserIdFromToken(accessToken);
//        authCommandService.logoutFromAllDevices(userId);
//
//        // 현재 세션의 쿠키 삭제
//        cookieUtil.deleteAllAuthCookies(httpResponse);
//
//        return ResponseEntity.ok(ApiResponse.success("모든 디바이스에서 로그아웃되었습니다."));
//    }
//
//    /**
//     * 인증 상태 확인
//     */
//    @GetMapping("/status")
//    @Operation(summary = "인증 상태 확인", description = "현재 로그인 상태를 확인합니다.")
//    public ResponseEntity<ApiResponse<Boolean>> checkAuthStatus(HttpServletRequest request) {
//        boolean hasAuthCookies = cookieUtil.hasAuthCookies(request);
//        return ResponseEntity.ok(ApiResponse.success("인증 상태 조회 완료", hasAuthCookies));
//    }
//
//    /**
//     * 인증 쿠키 설정 (내부 메서드)
//     */
//    private void setAuthCookies(HttpServletResponse response, AuthTokenResponse authResponse) {
//        cookieUtil.setAccessTokenCookie(response, authResponse.getAccessToken());
//        cookieUtil.setRefreshTokenCookie(response, authResponse.getRefreshToken());
//    }
//
//    /**
//     * 클라이언트 IP 주소 추출
//     */
//    private String getClientIpAddress(HttpServletRequest request) {
//        String xForwardedFor = request.getHeader("X-Forwarded-For");
//        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
//            return xForwardedFor.split(",")[0].trim();
//        }
//
//        String xRealIp = request.getHeader("X-Real-IP");
//        if (xRealIp != null && !xRealIp.isEmpty()) {
//            return xRealIp;
//        }
//
//        return request.getRemoteAddr();
//    }
//}