package com.stacknote.back.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stacknote.back.domain.user.dto.request.UserLoginRequest;
import com.stacknote.back.domain.user.dto.response.AuthTokenResponse;
import com.stacknote.back.domain.user.service.command.AuthCommandService;
import com.stacknote.back.global.utils.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * 커스텀 로그인 필터
 * JSON 형태의 로그인 요청을 처리하고 기존 AuthCommandService를 활용
 */
@Slf4j
@RequiredArgsConstructor
public class CustomLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthCommandService authCommandService;
    private final CookieUtil cookieUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 인증 시도 - JSON 요청 본문에서 로그인 정보 추출
     */
    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws AuthenticationException {

        log.info("Custom login filter - attempting authentication");

        try {
            // JSON 요청 본문에서 로그인 정보 추출
            UserLoginRequest loginRequest = objectMapper.readValue(
                    request.getInputStream(),
                    UserLoginRequest.class
            );

            // 기본 검증
            if (!StringUtils.hasText(loginRequest.getEmail()) ||
                    !StringUtils.hasText(loginRequest.getPassword())) {
                throw new IllegalArgumentException("이메일과 비밀번호는 필수입니다.");
            }

            log.info("로그인 시도: {}", loginRequest.getEmail());

            // UsernamePasswordAuthenticationToken 생성
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    );

            // 추가 정보를 details에 저장 (나중에 사용)
            authToken.setDetails(loginRequest);

            // AuthenticationManager에게 인증 위임
            return getAuthenticationManager().authenticate(authToken);

        } catch (IOException e) {
            log.error("로그인 요청 파싱 오류: {}", e.getMessage());
            throw new IllegalArgumentException("잘못된 요청 형식입니다.", e);
        }
    }

    /**
     * 인증 성공 시 처리 - 토큰 생성 및 쿠키 설정
     */
    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult
    ) throws IOException, ServletException {

        log.info("인증 성공 - 토큰 생성 시작");

        try {
            // 인증된 사용자 정보와 원본 요청 정보 추출
            String userEmail = authResult.getName();
            UserLoginRequest loginRequest = (UserLoginRequest) authResult.getDetails();

            // User-Agent와 IP 추출 (기존 Controller 로직 활용)
            String userAgent = request.getHeader("User-Agent");
            String ipAddress = getClientIpAddress(request);

            // 기존 AuthCommandService를 활용하여 토큰 생성
            AuthTokenResponse authResponse = authCommandService.login(
                    loginRequest, userAgent, ipAddress
            );

            // 토큰을 쿠키에 저장 (기존 로직 활용)
            setAuthCookies(response, authResponse);

            // 성공 핸들러로 위임하여 응답 처리
            getSuccessHandler().onAuthenticationSuccess(request, response, authResult);

        } catch (Exception e) {
            log.error("토큰 생성 오류: {}", e.getMessage(), e);

            // AuthenticationException으로 변환하여 실패 핸들러로 위임
            AuthenticationException authException;
            if (e instanceof AuthenticationException) {
                authException = (AuthenticationException) e;
            } else {
                // IllegalStateException 등을 AuthenticationException으로 변환
                authException = new org.springframework.security.authentication.AuthenticationServiceException(
                        "토큰 생성에 실패했습니다.", e
                );
            }

            getFailureHandler().onAuthenticationFailure(request, response, authException);
        }
    }

    /**
     * 인증 쿠키 설정 (기존 Controller 로직 재사용)
     */
    private void setAuthCookies(HttpServletResponse response, AuthTokenResponse authResponse) {
        cookieUtil.setAccessTokenCookie(response, authResponse.getAccessToken());
        cookieUtil.setRefreshTokenCookie(response, authResponse.getRefreshToken());
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
}