package com.stacknote.back.domain.page.exception;

import com.stacknote.back.global.exception.ErrorCode;
import com.stacknote.back.global.exception.custom.BusinessException;

/**
 * 페이지가 잠겨있어 편집할 수 없을 때 발생하는 예외
 */
public class PageLockedException extends BusinessException {

    public PageLockedException(String message) {
        super(ErrorCode.PAGE_ACCESS_DENIED, message);
    }

    public PageLockedException() {
        super(ErrorCode.PAGE_ACCESS_DENIED, "페이지가 잠겨있어 편집할 수 없습니다.");
    }
}