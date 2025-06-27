package com.stacknote.back.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stacknote.back.global.dto.ApiResponse;
import com.stacknote.back.global.dto.ErrorResponse;
import com.stacknote.back.global.exception.ErrorCode;
import com.stacknote.back.global.utils.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 토큰 갱신 실패 핸들러
 * 토큰 갱신 실패 시 JSON 응답 처리 및 쿠키 정리
 */
@Slf4j
@RequiredArgsConstructor
public class TokenReissueFailureHandler {

    private final CookieUtil cookieUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 토큰 갱신 실패 시 호출
     */
    public void onTokenReissueFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            Exception exception
    ) throws IOException {

        log.warn("토큰 갱신 실패: {}", exception.getMessage());

        // 유효하지 않은 토큰 쿠키들 삭제
        cookieUtil.deleteAllAuthCookies(response);

        String errorMessage = determineErrorMessage(exception);
        ErrorCode errorCode = determineErrorCode(exception);

        ErrorResponse errorResponse = new ErrorResponse(
                errorCode.getCode(),
                errorMessage
        );

        ApiResponse<ErrorResponse> apiResponse = ApiResponse.error(
                "토큰 갱신에 실패했습니다.",
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

        log.debug("토큰 갱신 실패 응답 전송 완료");
    }

    /**
     * 예외 타입에 따른 에러 메시지 결정
     */
    private String determineErrorMessage(Exception exception) {
        if (exception instanceof IllegalArgumentException) {
            return exception.getMessage();
        } else if (exception.getMessage() != null &&
                exception.getMessage().contains("expired")) {
            return "리프레시 토큰이 만료되었습니다. 다시 로그인해주세요.";
        } else if (exception.getMessage() != null &&
                exception.getMessage().contains("invalid")) {
            return "유효하지 않은 토큰입니다. 다시 로그인해주세요.";
        } else {
            return "토큰 갱신에 실패했습니다. 다시 로그인해주세요.";
        }
    }

    /**
     * 예외 타입에 따른 에러 코드 결정
     */
    private ErrorCode determineErrorCode(Exception exception) {
        if (exception instanceof IllegalArgumentException) {
            return ErrorCode.INVALID_INPUT;
        } else if (exception.getMessage() != null &&
                (exception.getMessage().contains("expired") ||
                        exception.getMessage().contains("invalid"))) {
            return ErrorCode.INVALID_TOKEN;
        } else {
            return ErrorCode.TOKEN_REFRESH_FAILED;
        }
    }
}