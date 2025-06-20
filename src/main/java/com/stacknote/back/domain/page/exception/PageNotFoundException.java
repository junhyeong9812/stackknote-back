package com.stacknote.back.domain.page.exception;

import com.stacknote.back.global.exception.ErrorCode;
import com.stacknote.back.global.exception.custom.EntityNotFoundException;

/**
 * 페이지를 찾을 수 없을 때 발생하는 예외
 */
public class PageNotFoundException extends EntityNotFoundException {

    public PageNotFoundException(String message) {
        super(ErrorCode.PAGE_NOT_FOUND, message);
    }

    public PageNotFoundException() {
        super(ErrorCode.PAGE_NOT_FOUND, "페이지를 찾을 수 없습니다.");
    }
}