package com.stacknote.back.domain.file.exception;

import com.stacknote.back.global.exception.ErrorCode;
import com.stacknote.back.global.exception.custom.BusinessException;

/**
 * 파일 크기가 제한을 초과했을 때 발생하는 예외
 */
public class FileSizeExceededException extends BusinessException {

    public FileSizeExceededException(String message) {
        super(ErrorCode.FILE_SIZE_EXCEEDED, message);
    }

    public FileSizeExceededException() {
        super(ErrorCode.FILE_SIZE_EXCEEDED, "파일 크기가 제한을 초과했습니다.");
    }
}