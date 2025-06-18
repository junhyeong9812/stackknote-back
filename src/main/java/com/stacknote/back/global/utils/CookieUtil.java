package com.stacknote.back.global.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

/**
 * 쿠키 관련 유틸리티 클래스
 * JWT 토큰을 쿠키에 저장/조회/삭제하는 기능 제공
 */
@Slf4j
@Component
public class CookieUtil {

    public static final String ACCESS_TOKEN_COOKIE_NAME = "stacknote_access_token";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "stacknote_refresh_token";

    @Value("${jwt.access-token-validity:30}")
    private long accessTokenValidityInMinutes;

    @Value("${jwt.refresh-token-validity:120}")
    private long refreshTokenValidityInMinutes;

    @Value("${app.domain:localhost}")
    private String domain;

    @Value("${app.secure-cookies:false}")
    private boolean secureCookies;

    /**
     * 액세스 토큰을 쿠키에 저장
     */
    public void setAccessTokenCookie(HttpServletResponse response, String accessToken) {
        Cookie cookie = createCookie(ACCESS_TOKEN_COOKIE_NAME, accessToken, (int) (accessTokenValidityInMinutes * 60));
        response.addCookie(cookie);
        log.debug("Access token cookie set");
    }

    /**
     * 리프레시 토큰을 쿠키에 저장
     */
    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = createCookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken, (int) (refreshTokenValidityInMinutes * 60));
        response.addCookie(cookie);
        log.debug("Refresh token cookie set");
    }

    /**
     * 요청에서 액세스 토큰 추출
     */
    public Optional<String> getAccessToken(HttpServletRequest request) {
        return getCookieValue(request, ACCESS_TOKEN_COOKIE_NAME);
    }

    /**
     * 요청에서 리프레시 토큰 추출
     */
    public Optional<String> getRefreshToken(HttpServletRequest request) {
        return getCookieValue(request, REFRESH_TOKEN_COOKIE_NAME);
    }

    /**
     * 액세스 토큰 쿠키 삭제
     */
    public void deleteAccessTokenCookie(HttpServletResponse response) {
        deleteCookie(response, ACCESS_TOKEN_COOKIE_NAME);
        log.debug("Access token cookie deleted");
    }

    /**
     * 리프레시 토큰 쿠키 삭제
     */
    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        deleteCookie(response, REFRESH_TOKEN_COOKIE_NAME);
        log.debug("Refresh token cookie deleted");
    }

    /**
     * 모든 인증 관련 쿠키 삭제
     */
    public void deleteAllAuthCookies(HttpServletResponse response) {
        deleteAccessTokenCookie(response);
        deleteRefreshTokenCookie(response);
        log.debug("All auth cookies deleted");
    }

    /**
     * 쿠키 생성
     */
    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);  // XSS 방지
        // cookie.setSecure(secureCookies);  // HTTPS에서만 전송 (개발 시 주석 처리)
        cookie.setPath("/");  // 모든 경로에서 접근 가능
        cookie.setMaxAge(maxAge);  // 만료 시간 설정

        // 도메인 설정 (localhost가 아닌 경우에만)
        if (!"localhost".equals(domain)) {
            cookie.setDomain(domain);
        }

        // SameSite 설정 (CSRF 방지) - Servlet 4.0+에서 지원
        // cookie.setAttribute("SameSite", "Strict");

        return cookie;
    }

    /**
     * 쿠키에서 값 추출
     */
    private Optional<String> getCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    /**
     * 쿠키 삭제 (maxAge를 0으로 설정)
     */
    private void deleteCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);  // 즉시 만료

        if (!"localhost".equals(domain)) {
            cookie.setDomain(domain);
        }

        response.addCookie(cookie);
    }

    /**
     * 쿠키 존재 여부 확인
     */
    public boolean hasCookie(HttpServletRequest request, String cookieName) {
        return getCookieValue(request, cookieName).isPresent();
    }

    /**
     * 모든 인증 쿠키 존재 여부 확인
     */
    public boolean hasAuthCookies(HttpServletRequest request) {
        return hasCookie(request, ACCESS_TOKEN_COOKIE_NAME) || hasCookie(request, REFRESH_TOKEN_COOKIE_NAME);
    }
}