package com.stacknote.back.domain.notification.exception;

import com.stacknote.back.global.exception.ErrorCode;
import com.stacknote.back.global.exception.custom.BusinessException;

/**
 * 알림에 대한 접근 권한이 없을 때 발생하는 예외
 */
public class NotificationAccessDeniedException extends BusinessException {

    public NotificationAccessDeniedException() {
        super(ErrorCode.ACCESS_DENIED, "알림에 대한 권한이 없습니다.");
    }

    public NotificationAccessDeniedException(String message) {
        super(ErrorCode.ACCESS_DENIED, message);
    }
}