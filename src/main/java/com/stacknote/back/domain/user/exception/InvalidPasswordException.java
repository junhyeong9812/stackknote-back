package com.stacknote.back.domain.user.exception;

/**
 * 비밀번호 관련 오류 시 발생하는 예외
 * (비밀번호 불일치, 잘못된 토큰 등)
 */
public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException(String message) {
        super(message);
    }

    public InvalidPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}