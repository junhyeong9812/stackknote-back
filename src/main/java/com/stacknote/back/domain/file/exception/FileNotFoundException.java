package com.stacknote.back.domain.file.exception;

import com.stacknote.back.global.exception.ErrorCode;
import com.stacknote.back.global.exception.custom.EntityNotFoundException;

/**
 * 파일을 찾을 수 없을 때 발생하는 예외
 */
public class FileNotFoundException extends EntityNotFoundException {

    public FileNotFoundException(String message) {
        super(ErrorCode.FILE_NOT_FOUND, message);
    }

    public FileNotFoundException() {
        super(ErrorCode.FILE_NOT_FOUND, "파일을 찾을 수 없습니다.");
    }
}