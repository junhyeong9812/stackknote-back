package com.stacknote.back.global.exception.custom;

import com.stacknote.back.global.exception.ErrorCode;
import lombok.Getter;

/**
 * 비즈니스 로직 예외의 기본 클래스
 * 모든 커스텀 비즈니스 예외는 이 클래스를 상속받아야 함
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}