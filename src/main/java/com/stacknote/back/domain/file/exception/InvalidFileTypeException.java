package com.stacknote.back.domain.file.exception;

import com.stacknote.back.global.exception.ErrorCode;
import com.stacknote.back.global.exception.custom.BusinessException;

/**
 * 지원하지 않는 파일 형식일 때 발생하는 예외
 */
public class InvalidFileTypeException extends BusinessException {

    public InvalidFileTypeException(String message) {
        super(ErrorCode.INVALID_FILE_TYPE, message);
    }

    public InvalidFileTypeException() {
        super(ErrorCode.INVALID_FILE_TYPE, "지원하지 않는 파일 형식입니다.");
    }
}