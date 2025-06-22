package com.stacknote.back.domain.notification.service.query;

import com.stacknote.back.domain.notification.dto.response.NotificationResponse;
import com.stacknote.back.domain.notification.dto.response.NotificationStatisticsResponse;
import com.stacknote.back.domain.notification.entity.Notification;
import com.stacknote.back.domain.notification.exception.NotificationNotFoundException;
import com.stacknote.back.domain.notification.repository.NotificationRepository;
import com.stacknote.back.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 알림 조회 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;

    /**
     * 알림 ID로 알림 조회
     */
    public NotificationResponse getNotificationById(Long notificationId) {
        Notification notification = notificationRepository.findActiveNotificationById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        return NotificationResponse.from(notification);
    }

    /**
     * 사용자의 모든 알림 조회 (최신순)
     */
    public List<NotificationResponse> getNotificationsByRecipient(User recipient, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Notification> notifications = notificationRepository.findNotificationsByRecipient(recipient, pageable);

        return notifications.stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 읽지 않은 알림 조회
     */
    public List<NotificationResponse> getUnreadNotificationsByRecipient(User recipient) {
        List<Notification> notifications = notificationRepository.findUnreadNotificationsByRecipient(recipient);

        return notifications.stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 읽지 않은 알림 수 조회
     */
    public long getUnreadNotificationCount(User recipient) {
        return notificationRepository.countUnreadNotificationsByRecipient(recipient);
    }

    /**
     * 사용자의 높은 우선순위 읽지 않은 알림 조회
     */
    public List<NotificationResponse> getHighPriorityUnreadNotifications(User recipient) {
        List<Notification> notifications = notificationRepository.findHighPriorityUnreadNotifications(recipient);

        return notifications.stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 알림 타입별 조회
     */
    public List<NotificationResponse> getNotificationsByType(
            User recipient, Notification.NotificationType type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Notification> notifications = notificationRepository.findNotificationsByRecipientAndType(recipient, type, pageable);

        return notifications.stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 기간 내 알림 조회
     */
    public List<NotificationResponse> getNotificationsByDateRange(
            User recipient, LocalDateTime startDate, LocalDateTime endDate) {
        List<Notification> notifications = notificationRepository.findNotificationsByRecipientAndDateRange(
                recipient, startDate, endDate);

        return notifications.stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 시스템 알림 조회
     */
    public List<NotificationResponse> getSystemNotificationsByRecipient(User recipient, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Notification> notifications = notificationRepository.findSystemNotificationsByRecipient(recipient, pageable);

        return notifications.stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 참조에 대한 알림 조회
     */
    public List<NotificationResponse> getNotificationsByReference(
            User recipient, String referenceType, Long referenceId) {
        List<Notification> notifications = notificationRepository.findNotificationsByReference(
                recipient, referenceType, referenceId);

        return notifications.stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 발송자별 알림 조회
     */
    public List<NotificationResponse> getNotificationsBySender(
            User recipient, User sender, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Notification> notifications = notificationRepository.findNotificationsBySender(recipient, sender, pageable);

        return notifications.stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 긴급 알림 조회
     */
    public List<NotificationResponse> getUrgentNotificationsByRecipient(User recipient) {
        List<Notification> notifications = notificationRepository.findUrgentNotificationsByRecipient(recipient);

        return notifications.stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 최근 N개 알림 조회
     */
    public List<NotificationResponse> getRecentNotificationsByRecipient(User recipient, int size) {
        Pageable pageable = PageRequest.of(0, size);
        List<Notification> notifications = notificationRepository.findRecentNotificationsByRecipient(recipient, pageable);

        return notifications.stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 알림 통계 조회
     */
    public NotificationStatisticsResponse getNotificationStatistics(User recipient) {
        Object[] statistics = notificationRepository.getNotificationStatistics(recipient);

        if (statistics != null && statistics.length >= 4) {
            return NotificationStatisticsResponse.builder()
                    .totalNotifications((Long) statistics[0])
                    .unreadNotifications((Long) statistics[1])
                    .highPriorityNotifications((Long) statistics[2])
                    .systemNotifications((Long) statistics[3])
                    .build();
        }

        return NotificationStatisticsResponse.builder()
                .totalNotifications(0L)
                .unreadNotifications(0L)
                .highPriorityNotifications(0L)
                .systemNotifications(0L)
                .build();
    }

    /**
     * 알림이 존재하는지 확인
     */
    public boolean existsNotification(Long notificationId) {
        return notificationRepository.findActiveNotificationById(notificationId).isPresent();
    }

    /**
     * 사용자가 알림 수신자인지 확인
     */
    public boolean isNotificationRecipient(Long notificationId, User user) {
        Notification notification = notificationRepository.findActiveNotificationById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        return notification.isRecipient(user);
    }

    /**
     * 중복 알림 확인
     */
    public boolean hasDuplicateNotifications(
            User recipient,
            Notification.NotificationType type,
            String referenceType,
            Long referenceId,
            User sender,
            LocalDateTime since) {

        List<Notification> duplicates = notificationRepository.findDuplicateNotifications(
                recipient, type, referenceType, referenceId, sender, since);

        return !duplicates.isEmpty();
    }
}