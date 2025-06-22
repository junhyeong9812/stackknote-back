package com.stacknote.back.domain.comment.exception;

import com.stacknote.back.global.exception.ErrorCode;
import com.stacknote.back.global.exception.custom.EntityNotFoundException;

/**
 * 댓글을 찾을 수 없을 때 발생하는 예외
 */
public class CommentNotFoundException extends EntityNotFoundException {

    public CommentNotFoundException() {
        super(ErrorCode.ENTITY_NOT_FOUND, "댓글을 찾을 수 없습니다.");
    }

    public CommentNotFoundException(Long commentId) {
        super(ErrorCode.ENTITY_NOT_FOUND, "댓글을 찾을 수 없습니다. ID: " + commentId);
    }

    public CommentNotFoundException(String message) {
        super(ErrorCode.ENTITY_NOT_FOUND, message);
    }
}