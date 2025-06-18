package com.stacknote.back.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stacknote.back.global.dto.ApiResponse;
import com.stacknote.back.global.dto.ErrorResponse;
import com.stacknote.back.global.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JWT 인증 실패 시 처리하는 EntryPoint
 * 인증되지 않은 사용자가 보호된 리소스에 접근할 때 호출됨
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        log.warn("Unauthorized access attempt: {} {}", request.getMethod(), request.getRequestURI());
        log.debug("Authentication exception: {}", authException.getMessage());

        // 에러 응답 생성
        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.UNAUTHORIZED.getCode(),
                "인증이 필요합니다. 로그인 후 다시 시도해주세요."
        );

        ApiResponse<ErrorResponse> apiResponse = ApiResponse.error(
                "인증이 필요합니다.",
                errorResponse
        );

        // HTTP 응답 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // JSON 응답 작성
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}