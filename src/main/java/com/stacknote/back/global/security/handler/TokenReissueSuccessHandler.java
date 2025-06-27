package com.stacknote.back.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stacknote.back.domain.user.dto.response.AuthTokenResponse;
import com.stacknote.back.global.dto.ApiResponse;
import com.stacknote.back.global.utils.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 토큰 갱신 성공 핸들러
 * 토큰 갱신 성공 시 JSON 응답 처리
 */
@Slf4j
@RequiredArgsConstructor
public class TokenReissueSuccessHandler {

    private final CookieUtil cookieUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 토큰 갱신 성공 시 호출
     */
    public void onTokenReissueSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthTokenResponse authResponse
    ) throws IOException {

        log.info("토큰 갱신 성공");

        // 응답 데이터 생성 (토큰 값은 제외하고 메타 정보만)
        // 실제 토큰 값은 쿠키에 이미 저장됨
        AuthTokenResponse responseData = AuthTokenResponse.of(
                authResponse.getAccessTokenExpiresAt(),
                authResponse.getRefreshTokenExpiresAt(),
                authResponse.getUser()
        );

        ApiResponse<AuthTokenResponse> apiResponse = ApiResponse.success(
                "토큰이 갱신되었습니다.",
                responseData
        );

        // HTTP 응답 설정
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // CORS 헤더 추가 (필요시)
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // JSON 응답 작성
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();

        log.debug("토큰 갱신 성공 응답 전송 완료");
    }
}