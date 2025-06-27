package com.stacknote.back.global.exception.custom;

import com.stacknote.back.global.exception.ErrorCode;

/**
 * 토큰 처리 중 발생하는 예외
 * 토큰 생성, 갱신, 검증 실패 시 사용
 */
public class TokenProcessingException extends BusinessException {

    public TokenProcessingException() {
        super(ErrorCode.TOKEN_GENERATION_FAILED);
    }

    public TokenProcessingException(String message) {
        super(ErrorCode.TOKEN_GENERATION_FAILED, message);
    }

    public TokenProcessingException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TokenProcessingException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public TokenProcessingException(Throwable cause) {
        super(ErrorCode.TOKEN_GENERATION_FAILED, cause);
    }

    public TokenProcessingException(String message, Throwable cause) {
        super(ErrorCode.TOKEN_GENERATION_FAILED, message, cause);
    }

    public TokenProcessingException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    /**
     * 토큰 갱신 실패용 팩토리 메서드
     */
    public static TokenProcessingException refreshFailed(String message) {
        return new TokenProcessingException(ErrorCode.TOKEN_REFRESH_FAILED, message);
    }

    public static TokenProcessingException refreshFailed(String message, Throwable cause) {
        return new TokenProcessingException(ErrorCode.TOKEN_REFRESH_FAILED, message, cause);
    }

    /**
     * 토큰 검증 실패용 팩토리 메서드
     */
    public static TokenProcessingException invalidToken(String message) {
        return new TokenProcessingException(ErrorCode.INVALID_TOKEN, message);
    }

    public static TokenProcessingException tokenExpired(String message) {
        return new TokenProcessingException(ErrorCode.TOKEN_EXPIRED, message);
    }
}