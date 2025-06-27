package com.stacknote.back.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stacknote.back.global.dto.ApiResponse;
import com.stacknote.back.global.dto.ErrorResponse;
import com.stacknote.back.global.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 커스텀 인증 실패 핸들러
 * 로그인 실패 시 JSON 응답 처리 및 상세한 에러 메시지 제공
 */
@Slf4j
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {

        log.warn("로그인 실패: {}", exception.getMessage());

        String errorMessage = determineErrorMessage(exception);
        ErrorCode errorCode = determineErrorCode(exception);

        ErrorResponse errorResponse = new ErrorResponse(
                errorCode.getCode(),
                errorMessage
        );

        ApiResponse<ErrorResponse> apiResponse = ApiResponse.error(
                "로그인에 실패했습니다.",
                errorResponse
        );

        // HTTP 응답 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // CORS 헤더 추가 (필요시)
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // JSON 응답 작성
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();

        log.debug("로그인 실패 응답 전송 완료");
    }

    /**
     * 예외 타입에 따른 에러 메시지 결정
     */
    private String determineErrorMessage(AuthenticationException exception) {
        if (exception instanceof BadCredentialsException) {
            return "이메일 또는 비밀번호가 올바르지 않습니다.";
        } else if (exception instanceof UsernameNotFoundException) {
            return "존재하지 않는 사용자입니다.";
        } else if (exception instanceof DisabledException) {
            return "비활성화된 계정입니다. 관리자에게 문의하세요.";
        } else if (exception instanceof LockedException) {
            return "잠긴 계정입니다. 관리자에게 문의하세요.";
        } else if (exception instanceof IllegalArgumentException) {
            return exception.getMessage(); // 커스텀 메시지 사용
        } else {
            return "로그인에 실패했습니다. 잠시 후 다시 시도해주세요.";
        }
    }

    /**
     * 예외 타입에 따른 에러 코드 결정
     */
    private ErrorCode determineErrorCode(AuthenticationException exception) {
        if (exception instanceof BadCredentialsException ||
                exception instanceof UsernameNotFoundException) {
            return ErrorCode.INVALID_CREDENTIALS;
        } else if (exception instanceof DisabledException ||
                exception instanceof LockedException) {
            return ErrorCode.ACCOUNT_DISABLED;
        } else if (exception instanceof IllegalArgumentException) {
            return ErrorCode.INVALID_INPUT;
        } else {
            return ErrorCode.AUTHENTICATION_FAILED;
        }
    }
}