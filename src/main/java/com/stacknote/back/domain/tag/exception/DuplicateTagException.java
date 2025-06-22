package com.stacknote.back.domain.tag.exception;

import com.stacknote.back.global.exception.ErrorCode;
import com.stacknote.back.global.exception.custom.DuplicateResourceException;

/**
 * 중복된 태그명일 때 발생하는 예외
 */
public class DuplicateTagException extends DuplicateResourceException {

    public DuplicateTagException() {
        super(ErrorCode.DUPLICATE_EMAIL, "이미 존재하는 태그명입니다.");
    }

    public DuplicateTagException(String tagName) {
        super(ErrorCode.DUPLICATE_EMAIL, "이미 존재하는 태그명입니다: " + tagName);
    }

    public DuplicateTagException(String message, boolean isCustomMessage) {
        super(ErrorCode.DUPLICATE_EMAIL, message);
    }
}