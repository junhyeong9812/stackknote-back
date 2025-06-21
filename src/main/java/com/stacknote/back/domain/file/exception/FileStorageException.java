package com.stacknote.back.domain.file.exception;

import com.stacknote.back.global.exception.ErrorCode;
import com.stacknote.back.global.exception.custom.BusinessException;

/**
 * 파일 저장소 관련 오류 시 발생하는 예외
 */
public class FileStorageException extends BusinessException {

    public FileStorageException(String message) {
        super(ErrorCode.FILE_UPLOAD_FAILED, message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(ErrorCode.FILE_UPLOAD_FAILED, message);
        initCause(cause);
    }

    public FileStorageException() {
        super(ErrorCode.FILE_UPLOAD_FAILED, "파일 저장소 오류가 발생했습니다.");
    }
}