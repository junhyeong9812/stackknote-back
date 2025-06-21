package com.stacknote.back.domain.file.exception;

import com.stacknote.back.global.exception.ErrorCode;
import com.stacknote.back.global.exception.custom.BusinessException;

/**
 * 파일 업로드 실패 시 발생하는 예외
 */
public class FileUploadException extends BusinessException {

    public FileUploadException(String message) {
        super(ErrorCode.FILE_UPLOAD_FAILED, message);
    }

    public FileUploadException() {
        super(ErrorCode.FILE_UPLOAD_FAILED, "파일 업로드에 실패했습니다.");
    }
}