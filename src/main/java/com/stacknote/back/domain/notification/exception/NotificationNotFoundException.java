package com.stacknote.back.domain.notification.exception;

import com.stacknote.back.global.exception.ErrorCode;
import com.stacknote.back.global.exception.custom.EntityNotFoundException;

/**
 * 알림을 찾을 수 없을 때 발생하는 예외
 */
public class NotificationNotFoundException extends EntityNotFoundException {

    public NotificationNotFoundException() {
        super(ErrorCode.ENTITY_NOT_FOUND, "알림을 찾을 수 없습니다.");
    }

    public NotificationNotFoundException(Long notificationId) {
        super(ErrorCode.ENTITY_NOT_FOUND, "알림을 찾을 수 없습니다. ID: " + notificationId);
    }

    public NotificationNotFoundException(String message) {
        super(ErrorCode.ENTITY_NOT_FOUND, message);
    }
}