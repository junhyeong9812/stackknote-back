package com.stacknote.back.domain.search.exception;

import com.stacknote.back.global.exception.ErrorCode;
import com.stacknote.back.global.exception.custom.BusinessException;

/**
 * 검색 서비스를 사용할 수 없을 때 발생하는 예외
 */
public class SearchNotAvailableException extends BusinessException {

    public SearchNotAvailableException(String message) {
        super(ErrorCode.INTERNAL_SERVER_ERROR, message);
    }

    public SearchNotAvailableException() {
        super(ErrorCode.INTERNAL_SERVER_ERROR, "검색 서비스를 사용할 수 없습니다.");
    }
}