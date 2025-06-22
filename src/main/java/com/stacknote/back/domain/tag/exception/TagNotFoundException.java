package com.stacknote.back.domain.tag.exception;

import com.stacknote.back.global.exception.ErrorCode;
import com.stacknote.back.global.exception.custom.EntityNotFoundException;

/**
 * 태그를 찾을 수 없을 때 발생하는 예외
 */
public class TagNotFoundException extends EntityNotFoundException {

    public TagNotFoundException() {
        super(ErrorCode.ENTITY_NOT_FOUND, "태그를 찾을 수 없습니다.");
    }

    public TagNotFoundException(Long tagId) {
        super(ErrorCode.ENTITY_NOT_FOUND, "태그를 찾을 수 없습니다. ID: " + tagId);
    }

    public TagNotFoundException(String tagName) {
        super(ErrorCode.ENTITY_NOT_FOUND, "태그를 찾을 수 없습니다. 이름: " + tagName);
    }

    public TagNotFoundException(String message, boolean isCustomMessage) {
        super(ErrorCode.ENTITY_NOT_FOUND, message);
    }
}