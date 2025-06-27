package com.stacknote.back.global.exception.custom;

import com.stacknote.back.global.exception.ErrorCode;

/**
 * 인증 처리 중 발생하는 예외
 * Filter에서 토큰 생성, 갱신 실패 시 사용
 */
public class AuthenticationProcessingException extends BusinessException {

    public AuthenticationProcessingException() {
        super(ErrorCode.AUTHENTICATION_PROCESSING_ERROR);
    }

    public AuthenticationProcessingException(String message) {
        super(ErrorCode.AUTHENTICATION_PROCESSING_ERROR, message);
    }

    public AuthenticationProcessingException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AuthenticationProcessingException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public AuthenticationProcessingException(Throwable cause) {
        super(ErrorCode.AUTHENTICATION_PROCESSING_ERROR, cause);
    }

    public AuthenticationProcessingException(String message, Throwable cause) {
        super(ErrorCode.AUTHENTICATION_PROCESSING_ERROR, message, cause);
    }
}