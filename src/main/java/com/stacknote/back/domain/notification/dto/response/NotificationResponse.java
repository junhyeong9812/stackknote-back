package com.stacknote.back.domain.notification.dto.response;

import com.stacknote.back.domain.notification.entity.Notification;
import com.stacknote.back.domain.user.dto.response.UserResponse;
import com.stacknote.back.global.utils.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 알림 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private String type;
    private String typeDescription;
    private String title;
    private String content;
    private UserResponse recipient;
    private UserResponse sender; // 시스템 알림인 경우 null
    private Boolean isRead;
    private String referenceType;
    private Long referenceId;
    private String actionUrl;
    private String metadata;
    private String priority;
    private String priorityDescription;
    private String relativeTime; // "5분 전" 형태
    private Boolean isSystemNotification;
    private Boolean isUrgent;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Notification 엔티티로부터 NotificationResponse 생성
     */
    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType().name())
                .typeDescription(notification.getType().getDescription())
                .title(notification.getTitle())
                .content(notification.getContent())
                .recipient(UserResponse.from(notification.getRecipient()))
                .sender(notification.getSender() != null ? UserResponse.from(notification.getSender()) : null)
                .isRead(notification.getIsRead())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .actionUrl(notification.getActionUrl())
                .metadata(notification.getMetadata())
                .priority(notification.getPriority().name())
                .priorityDescription(notification.getPriority().getDescription())
                .relativeTime(DateUtil.getRelativeTime(notification.getCreatedAt()))
                .isSystemNotification(notification.isSystemNotification())
                .isUrgent(notification.isUrgent())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
}