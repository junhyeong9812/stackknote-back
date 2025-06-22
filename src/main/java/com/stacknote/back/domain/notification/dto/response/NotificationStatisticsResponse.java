package com.stacknote.back.domain.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 알림 통계 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationStatisticsResponse {

    private Long totalNotifications;        // 전체 알림 수
    private Long unreadNotifications;       // 읽지 않은 알림 수
    private Long highPriorityNotifications; // 높은 우선순위 알림 수
    private Long systemNotifications;       // 시스템 알림 수
}