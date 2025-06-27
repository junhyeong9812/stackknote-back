package com.stacknote.back.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stacknote.back.global.dto.ApiResponse;
import com.stacknote.back.global.utils.CookieUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 커스텀 인증 성공 핸들러
 * 로그인 성공 시 JSON 응답 처리
 */
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final CookieUtil cookieUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        log.info("로그인 성공 - 사용자: {}", authentication.getName());

        // 성공 응답 생성
        // 토큰 값은 쿠키에 저장되어 있으므로 메타 정보만 반환
        ApiResponse<String> apiResponse = ApiResponse.success(
                "로그인이 완료되었습니다.",
                "인증 토큰이 쿠키에 저장되었습니다."
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

        log.debug("로그인 성공 응답 전송 완료");
    }
}