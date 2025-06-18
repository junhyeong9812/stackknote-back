package com.stacknote.back.global.exception.custom;

import com.stacknote.back.global.exception.ErrorCode;

/**
 * 리소스 중복 시 발생하는 예외
 */
public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException() {
        super(ErrorCode.DUPLICATE_EMAIL);
    }

    public DuplicateResourceException(String message) {
        super(ErrorCode.DUPLICATE_EMAIL, message);
    }

    public DuplicateResourceException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DuplicateResourceException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}