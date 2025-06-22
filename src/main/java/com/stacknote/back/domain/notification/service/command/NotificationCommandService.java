package com.stacknote.back.domain.notification.service.command;

import com.stacknote.back.domain.notification.dto.request.NotificationCreateRequest;
import com.stacknote.back.domain.notification.dto.request.NotificationUpdateRequest;
import com.stacknote.back.domain.notification.dto.response.NotificationResponse;
import com.stacknote.back.domain.notification.entity.Notification;
import com.stacknote.back.domain.notification.exception.NotificationAccessDeniedException;
import com.stacknote.back.domain.notification.exception.NotificationNotFoundException;
import com.stacknote.back.domain.notification.repository.NotificationRepository;
import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.domain.user.repository.UserRepository;
import com.stacknote.back.global.exception.custom.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 알림 명령 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationCommandService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * 알림 생성
     */
    public NotificationResponse createNotification(NotificationCreateRequest request) {
        // 수신자 확인
        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new EntityNotFoundException("수신자를 찾을 수 없습니다."));

        User sender = null;
        if (request.getSenderId() != null) {
            sender = userRepository.findById(request.getSenderId())
                    .orElseThrow(() -> new EntityNotFoundException("발송자를 찾을 수 없습니다."));
        }

        Notification notification = Notification.builder()
                .type(request.getType())
                .title(request.getTitle())
                .content(request.getContent())
                .recipient(recipient)
                .sender(sender)
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .actionUrl(request.getActionUrl())
                .metadata(request.getMetadata())
                .priority(request.getPriority())
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        log.info("알림 생성 완료 - ID: {}, 타입: {}, 수신자: {}",
                savedNotification.getId(), savedNotification.getType(), recipient.getEmail());

        return NotificationResponse.from(savedNotification);
    }

    /**
     * 알림 수정
     */
    public NotificationResponse updateNotification(Long notificationId, NotificationUpdateRequest request, User user) {
        Notification notification = notificationRepository.findActiveNotificationById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        // 시스템 알림은 수정 불가
        if (notification.isSystemNotification()) {
            throw new IllegalArgumentException("시스템 알림은 수정할 수 없습니다.");
        }

        // 발송자만 수정 가능
        if (notification.getSender() == null || !notification.isSender(user)) {
            throw new NotificationAccessDeniedException("알림을 수정할 권한이 없습니다.");
        }

        notification.updateContent(request.getTitle(), request.getContent());

        if (request.getActionUrl() != null) {
            notification.setActionUrl(request.getActionUrl());
        }

        if (request.getMetadata() != null) {
            notification.setMetadata(request.getMetadata());
        }

        log.info("알림 수정 완료 - ID: {}, 수정자: {}", notificationId, user.getEmail());

        return NotificationResponse.from(notification);
    }

    /**
     * 알림 읽음 처리
     */
    public void markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findActiveNotificationById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        // 수신자만 읽음 처리 가능
        if (!notification.isRecipient(user)) {
            throw new NotificationAccessDeniedException("알림에 대한 권한이 없습니다.");
        }

        notification.markAsRead();

        log.debug("알림 읽음 처리 - ID: {}, 사용자: {}", notificationId, user.getEmail());
    }

    /**
     * 알림 읽지 않음 처리
     */
    public void markAsUnread(Long notificationId, User user) {
        Notification notification = notificationRepository.findActiveNotificationById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        // 수신자만 읽지 않음 처리 가능
        if (!notification.isRecipient(user)) {
            throw new NotificationAccessDeniedException("알림에 대한 권한이 없습니다.");
        }

        notification.markAsUnread();

        log.debug("알림 읽지 않음 처리 - ID: {}, 사용자: {}", notificationId, user.getEmail());
    }

    /**
     * 모든 알림 읽음 처리
     */
    public void markAllAsRead(User user) {
        int updatedCount = notificationRepository.markAllAsReadByRecipient(user);

        log.info("모든 알림 읽음 처리 완료 - 사용자: {}, 처리된 알림 수: {}", user.getEmail(), updatedCount);
    }

    /**
     * 특정 타입의 모든 알림 읽음 처리
     */
    public void markAsReadByType(User user, Notification.NotificationType type) {
        int updatedCount = notificationRepository.markAsReadByRecipientAndType(user, type);

        log.info("타입별 알림 읽음 처리 완료 - 사용자: {}, 타입: {}, 처리된 알림 수: {}",
                user.getEmail(), type, updatedCount);
    }

    /**
     * 특정 참조에 대한 모든 알림 읽음 처리
     */
    public void markAsReadByReference(User user, String referenceType, Long referenceId) {
        int updatedCount = notificationRepository.markAsReadByReference(user, referenceType, referenceId);

        log.info("참조별 알림 읽음 처리 완료 - 사용자: {}, 참조: {}:{}, 처리된 알림 수: {}",
                user.getEmail(), referenceType, referenceId, updatedCount);
    }

    /**
     * 알림 삭제 (소프트 삭제)
     */
    public void deleteNotification(Long notificationId, User user) {
        Notification notification = notificationRepository.findActiveNotificationById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        // 수신자만 삭제 가능
        if (!notification.isRecipient(user)) {
            throw new NotificationAccessDeniedException("알림을 삭제할 권한이 없습니다.");
        }

        notification.delete();

        log.info("알림 삭제 완료 - ID: {}, 사용자: {}", notificationId, user.getEmail());
    }

    /**
     * 사용자의 모든 알림 삭제
     */
    public void deleteAllNotifications(User user) {
        int deletedCount = notificationRepository.softDeleteNotificationsByRecipient(user);

        log.info("모든 알림 삭제 완료 - 사용자: {}, 삭제된 알림 수: {}", user.getEmail(), deletedCount);
    }

    /**
     * 특정 타입의 알림 삭제
     */
    public void deleteNotificationsByType(User user, Notification.NotificationType type) {
        int deletedCount = notificationRepository.softDeleteNotificationsByType(user, type);

        log.info("타입별 알림 삭제 완료 - 사용자: {}, 타입: {}, 삭제된 알림 수: {}",
                user.getEmail(), type, deletedCount);
    }

    /**
     * 만료된 알림 정리 (배치 작업용)
     */
    public void cleanupExpiredNotifications() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        int deletedCount = notificationRepository.deleteExpiredNotifications(cutoffDate);

        log.info("만료된 알림 정리 완료 - 삭제된 알림 수: {}", deletedCount);
    }

    /**
     * 읽은 알림 정리 (사용자당 최대 1000개 유지)
     */
    public void cleanupOldReadNotifications(User user) {
        int keepCount = 1000;
        List<Long> oldNotificationIds = notificationRepository.findOldReadNotificationIds(
                user.getId(), keepCount, Integer.MAX_VALUE);

        if (!oldNotificationIds.isEmpty()) {
            int deletedCount = notificationRepository.deleteNotificationsByIds(oldNotificationIds);
            log.info("오래된 읽은 알림 정리 완료 - 사용자: {}, 삭제된 알림 수: {}",
                    user.getEmail(), deletedCount);
        }
    }

    /**
     * 워크스페이스 초대 알림 생성
     */
    public NotificationResponse createWorkspaceInvitationNotification(
            User recipient, User sender, String workspaceName, Long workspaceId) {

        Notification notification = Notification.createWorkspaceInvitation(
                recipient, sender, workspaceName, workspaceId);

        Notification saved = notificationRepository.save(notification);

        log.info("워크스페이스 초대 알림 생성 - 수신자: {}, 워크스페이스: {}",
                recipient.getEmail(), workspaceName);

        return NotificationResponse.from(saved);
    }

    /**
     * 페이지 댓글 알림 생성
     */
    public NotificationResponse createPageCommentNotification(
            User recipient, User sender, String pageTitle, Long pageId, Long commentId) {

        Notification notification = Notification.createPageComment(
                recipient, sender, pageTitle, pageId, commentId);

        Notification saved = notificationRepository.save(notification);

        log.info("페이지 댓글 알림 생성 - 수신자: {}, 페이지: {}",
                recipient.getEmail(), pageTitle);

        return NotificationResponse.from(saved);
    }

    /**
     * 댓글 답글 알림 생성
     */
    public NotificationResponse createCommentReplyNotification(
            User recipient, User sender, String pageTitle, Long commentId) {

        Notification notification = Notification.createCommentReply(
                recipient, sender, pageTitle, commentId);

        Notification saved = notificationRepository.save(notification);

        log.info("댓글 답글 알림 생성 - 수신자: {}, 페이지: {}",
                recipient.getEmail(), pageTitle);

        return NotificationResponse.from(saved);
    }

    /**
     * 멘션 알림 생성
     */
    public NotificationResponse createMentionNotification(
            User recipient, User sender, String pageTitle, Long pageId, String context) {

        Notification notification = Notification.createMention(
                recipient, sender, pageTitle, pageId, context);

        Notification saved = notificationRepository.save(notification);

        log.info("멘션 알림 생성 - 수신자: {}, 페이지: {}",
                recipient.getEmail(), pageTitle);

        return NotificationResponse.from(saved);
    }

    /**
     * 시스템 공지 알림 생성
     */
    public NotificationResponse createSystemAnnouncementNotification(
            User recipient, String title, String content) {

        Notification notification = Notification.createSystemAnnouncement(recipient, title, content);

        Notification saved = notificationRepository.save(notification);

        log.info("시스템 공지 알림 생성 - 수신자: {}, 제목: {}",
                recipient.getEmail(), title);

        return NotificationResponse.from(saved);
    }

    /**
     * 보안 알림 생성
     */
    public NotificationResponse createSecurityAlertNotification(User recipient, String message) {
        Notification notification = Notification.createSecurityAlert(recipient, message);

        Notification saved = notificationRepository.save(notification);

        log.info("보안 알림 생성 - 수신자: {}", recipient.getEmail());

        return NotificationResponse.from(saved);
    }
}