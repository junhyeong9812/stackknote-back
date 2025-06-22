package com.stacknote.back.domain.tag.exception;

import com.stacknote.back.global.exception.ErrorCode;
import com.stacknote.back.global.exception.custom.BusinessException;

/**
 * 사용 중인 태그를 삭제하려 할 때 발생하는 예외
 */
public class TagInUseException extends BusinessException {

    public TagInUseException() {
        super(ErrorCode.INVALID_REQUEST, "사용 중인 태그는 삭제할 수 없습니다.");
    }

    public TagInUseException(String tagName) {
        super(ErrorCode.INVALID_REQUEST, "사용 중인 태그는 삭제할 수 없습니다: " + tagName);
    }

    public TagInUseException(String message, boolean isCustomMessage) {
        super(ErrorCode.INVALID_REQUEST, message);
    }
}