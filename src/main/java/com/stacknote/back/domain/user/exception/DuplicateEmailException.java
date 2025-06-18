package com.stacknote.back.domain.user.exception;

/**
 * 이메일 또는 사용자명 중복 시 발생하는 예외
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) {
        super(message);
    }

    public DuplicateEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}