package com.stacknote.back.domain.page.exception;

import com.stacknote.back.global.exception.ErrorCode;
import com.stacknote.back.global.exception.custom.BusinessException;

/**
 * 페이지 접근 권한이 없을 때 발생하는 예외
 */
public class PageAccessDeniedException extends BusinessException {

    public PageAccessDeniedException(String message) {
        super(ErrorCode.PAGE_ACCESS_DENIED, message);
    }

    public PageAccessDeniedException() {
        super(ErrorCode.PAGE_ACCESS_DENIED, "페이지 접근 권한이 없습니다.");
    }
}