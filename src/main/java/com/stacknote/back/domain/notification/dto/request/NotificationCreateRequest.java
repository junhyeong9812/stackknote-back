package com.stacknote.back.domain.notification.dto.request;

import com.stacknote.back.domain.notification.entity.Notification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 알림 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCreateRequest {

    @NotNull(message = "알림 타입은 필수입니다.")
    private Notification.NotificationType type;

    @NotBlank(message = "알림 제목은 필수입니다.")
    @Size(max = 255, message = "알림 제목은 255자를 초과할 수 없습니다.")
    private String title;

    @NotBlank(message = "알림 내용은 필수입니다.")
    @Size(max = 1000, message = "알림 내용은 1000자를 초과할 수 없습니다.")
    private String content;

    @NotNull(message = "수신자 ID는 필수입니다.")
    private Long recipientId;

    private Long senderId; // 시스템 알림인 경우 null

    private String referenceType; // 참조 타입

    private Long referenceId; // 참조 ID

    @Size(max = 500, message = "액션 URL은 500자를 초과할 수 없습니다.")
    private String actionUrl;

    @Size(max = 1000, message = "메타데이터는 1000자를 초과할 수 없습니다.")
    private String metadata;

    private Notification.Priority priority = Notification.Priority.NORMAL;
}