package com.stacknote.back.domain.comment.exception;

import com.stacknote.back.global.exception.ErrorCode;
import com.stacknote.back.global.exception.custom.BusinessException;

/**
 * 댓글에 대한 접근 권한이 없을 때 발생하는 예외
 */
public class CommentAccessDeniedException extends BusinessException {

    public CommentAccessDeniedException() {
        super(ErrorCode.ACCESS_DENIED, "댓글에 대한 권한이 없습니다.");
    }

    public CommentAccessDeniedException(String message) {
        super(ErrorCode.ACCESS_DENIED, message);
    }
}