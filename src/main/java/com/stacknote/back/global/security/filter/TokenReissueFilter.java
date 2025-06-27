package com.stacknote.back.global.security.filter;

import com.stacknote.back.domain.user.dto.response.AuthTokenResponse;
import com.stacknote.back.domain.user.service.command.AuthCommandService;
import com.stacknote.back.global.security.handler.TokenReissueFailureHandler;
import com.stacknote.back.global.security.handler.TokenReissueSuccessHandler;
import com.stacknote.back.global.utils.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 토큰 갱신 처리 필터
 * POST /api/auth/refresh 요청을 가로채서 토큰 갱신 처리
 */
@Slf4j
@RequiredArgsConstructor
public class TokenReissueFilter extends OncePerRequestFilter {

    private final String reissueUrl;
    private final AuthCommandService authCommandService;
    private final CookieUtil cookieUtil;

    private TokenReissueSuccessHandler successHandler;
    private TokenReissueFailureHandler failureHandler;

    public void setSuccessHandler(TokenReissueSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    public void setFailureHandler(TokenReissueFailureHandler failureHandler) {
        this.failureHandler = failureHandler;
    }

    /**
     * 필터 실행 로직
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 토큰 갱신 URL이 아니면 다음 필터로 패스
        if (!matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        log.info("토큰 갱신 요청 처리 시작");

        try {
            // 쿠키에서 리프레시 토큰 추출
            String refreshToken = cookieUtil.getRefreshToken(request)
                    .orElseThrow(() -> new IllegalArgumentException("리프레시 토큰이 없습니다."));

            // User-Agent와 IP 주소 추출 (기존 로직 활용)
            String userAgent = request.getHeader("User-Agent");
            String ipAddress = getClientIpAddress(request);

            // 기존 AuthCommandService 활용하여 토큰 갱신
            AuthTokenResponse authResponse = authCommandService.refreshToken(
                    refreshToken, userAgent, ipAddress
            );

            // 새로운 액세스 토큰을 쿠키에 저장
            cookieUtil.setAccessTokenCookie(response, authResponse.getAccessToken());

            // 성공 핸들러 호출
            if (successHandler != null) {
                successHandler.onTokenReissueSuccess(request, response, authResponse);
            }

            log.info("토큰 갱신 성공");

        } catch (Exception e) {
            log.error("토큰 갱신 실패: {}", e.getMessage(), e);

            // 실패 핸들러 호출
            if (failureHandler != null) {
                failureHandler.onTokenReissueFailure(request, response, e);
            } else {
                // 핸들러가 없으면 기본 에러 응답
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"토큰 갱신에 실패했습니다.\"}");
                response.getWriter().flush();
            }
        }

        // 토큰 갱신 처리 완료 후 더 이상 필터 체인 진행하지 않음
        // (응답이 이미 완료됨)
    }

    /**
     * 요청이 토큰 갱신 URL과 일치하는지 확인
     */
    private boolean matches(HttpServletRequest request) {
        return "POST".equals(request.getMethod()) &&
                reissueUrl.equals(request.getRequestURI());
    }

    /**
     * 클라이언트 IP 주소 추출 (기존 Controller 로직 재사용)
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * 이 필터를 적용하지 않을 요청 판단
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // 토큰 갱신 URL이 아닌 경우 필터 적용하지 않음
        return !matches(request);
    }
}