package com.stacknote.back.domain.comment.exception;

import com.stacknote.back.global.exception.ErrorCode;
import com.stacknote.back.global.exception.custom.BusinessException;

/**
 * 유효하지 않은 댓글 정보일 때 발생하는 예외
 */
public class InvalidCommentException extends BusinessException {

    public InvalidCommentException() {
        super(ErrorCode.INVALID_INPUT_VALUE, "유효하지 않은 댓글 정보입니다.");
    }

    public InvalidCommentException(String message) {
        super(ErrorCode.INVALID_INPUT_VALUE, message);
    }
}