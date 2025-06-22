package com.stacknote.back.domain.notification.entity;

import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 알림 엔티티
 * 사용자에게 전송되는 다양한 알림을 관리
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_recipient", columnList = "recipient_id"),
        @Index(name = "idx_notification_type", columnList = "type"),
        @Index(name = "idx_notification_read", columnList = "is_read"),
        @Index(name = "idx_notification_created_at", columnList = "created_at")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type; // 알림 타입

    @Column(name = "title", nullable = false, length = 255)
    private String title; // 알림 제목

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content; // 알림 내용

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient; // 알림 수신자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender; // 알림 발송자 (시스템 알림인 경우 null)

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false; // 읽음 여부

    @Column(name = "reference_type", length = 50)
    private String referenceType; // 참조 타입 (PAGE, WORKSPACE, COMMENT 등)

    @Column(name = "reference_id")
    private Long referenceId; // 참조 ID

    @Column(name = "action_url", length = 500)
    private String actionUrl; // 클릭 시 이동할 URL

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // 추가 메타데이터 (JSON 형태)

    @Column(name = "priority")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Priority priority = Priority.NORMAL; // 알림 우선순위

    @Column(name = "read_at")
    private java.time.LocalDateTime readAt; // 읽은 시간

    @Column(name = "deleted_at")
    private java.time.LocalDateTime deletedAt; // 삭제 시간 (소프트 삭제)

    // ===== 알림 타입 열거형 =====
    public enum NotificationType {
        // 워크스페이스 관련
        WORKSPACE_INVITATION("워크스페이스 초대"),
        WORKSPACE_MEMBER_JOINED("워크스페이스 멤버 참가"),
        WORKSPACE_ROLE_CHANGED("워크스페이스 역할 변경"),

        // 페이지 관련
        PAGE_SHARED("페이지 공유"),
        PAGE_COMMENTED("페이지 댓글"),
        PAGE_MENTIONED("페이지 멘션"),
        PAGE_UPDATED("페이지 업데이트"),

        // 댓글 관련
        COMMENT_REPLY("댓글 답글"),
        COMMENT_LIKED("댓글 좋아요"),
        COMMENT_MENTIONED("댓글 멘션"),

        // 시스템 관련
        SYSTEM_ANNOUNCEMENT("시스템 공지"),
        SECURITY_ALERT("보안 알림"),
        MAINTENANCE_NOTICE("점검 안내"),

        // 계정 관련
        PASSWORD_CHANGED("비밀번호 변경"),
        EMAIL_VERIFIED("이메일 인증"),
        LOGIN_ALERT("로그인 알림");

        private final String description;

        NotificationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // ===== 우선순위 열거형 =====
    public enum Priority {
        LOW("낮음"),
        NORMAL("보통"),
        HIGH("높음"),
        URGENT("긴급");

        private final String description;

        Priority(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // ===== 비즈니스 로직 메서드 =====

    /**
     * 알림 읽음 처리
     */
    public void markAsRead() {
        if (!this.isRead) {
            this.isRead = true;
            this.readAt = java.time.LocalDateTime.now();
        }
    }

    /**
     * 알림 읽지 않음 처리
     */
    public void markAsUnread() {
        this.isRead = false;
        this.readAt = null;
    }

    /**
     * 시스템 알림인지 확인
     */
    public boolean isSystemNotification() {
        return this.sender == null;
    }

    /**
     * 긴급 알림인지 확인
     */
    public boolean isUrgent() {
        return this.priority == Priority.URGENT;
    }

    /**
     * 높은 우선순위 알림인지 확인
     */
    public boolean isHighPriority() {
        return this.priority == Priority.HIGH || this.priority == Priority.URGENT;
    }

    /**
     * 알림 내용 업데이트
     */
    public void updateContent(String title, String content) {
        if (title != null && !title.trim().isEmpty()) {
            this.title = title.trim();
        }
        if (content != null && !content.trim().isEmpty()) {
            this.content = content.trim();
        }
    }

    /**
     * 액션 URL 설정
     */
    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    /**
     * 메타데이터 설정
     */
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    /**
     * 우선순위 설정
     */
    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    /**
     * 참조 정보 설정
     */
    public void setReference(String referenceType, Long referenceId) {
        this.referenceType = referenceType;
        this.referenceId = referenceId;
    }

    /**
     * 알림 수신자인지 확인
     */
    public boolean isRecipient(User user) {
        return this.recipient.getId().equals(user.getId());
    }

    /**
     * 알림 발송자인지 확인
     */
    public boolean isSender(User user) {
        return this.sender != null && this.sender.getId().equals(user.getId());
    }

    /**
     * 알림이 만료되었는지 확인 (생성 후 30일)
     */
    public boolean isExpired() {
        return this.getCreatedAt().isBefore(java.time.LocalDateTime.now().minusDays(30));
    }

    /**
     * 소프트 삭제
     */
    public void delete() {
        this.deletedAt = java.time.LocalDateTime.now();
    }

    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * 알림 생성 팩토리 메서드들
     */

    /**
     * 워크스페이스 초대 알림 생성
     */
    public static Notification createWorkspaceInvitation(User recipient, User sender, String workspaceName, Long workspaceId) {
        return Notification.builder()
                .type(NotificationType.WORKSPACE_INVITATION)
                .title("워크스페이스 초대")
                .content(sender.getUsername() + "님이 '" + workspaceName + "' 워크스페이스에 초대했습니다.")
                .recipient(recipient)
                .sender(sender)
                .referenceType("WORKSPACE")
                .referenceId(workspaceId)
                .priority(Priority.HIGH)
                .build();
    }

    /**
     * 페이지 댓글 알림 생성
     */
    public static Notification createPageComment(User recipient, User sender, String pageTitle, Long pageId, Long commentId) {
        return Notification.builder()
                .type(NotificationType.PAGE_COMMENTED)
                .title("새 댓글")
                .content(sender.getUsername() + "님이 '" + pageTitle + "' 페이지에 댓글을 남겼습니다.")
                .recipient(recipient)
                .sender(sender)
                .referenceType("PAGE")
                .referenceId(pageId)
                .metadata("{\"commentId\":" + commentId + "}")
                .priority(Priority.NORMAL)
                .build();
    }

    /**
     * 댓글 답글 알림 생성
     */
    public static Notification createCommentReply(User recipient, User sender, String pageTitle, Long commentId) {
        return Notification.builder()
                .type(NotificationType.COMMENT_REPLY)
                .title("댓글 답글")
                .content(sender.getUsername() + "님이 당신의 댓글에 답글을 남겼습니다.")
                .recipient(recipient)
                .sender(sender)
                .referenceType("COMMENT")
                .referenceId(commentId)
                .priority(Priority.NORMAL)
                .build();
    }

    /**
     * 멘션 알림 생성
     */
    public static Notification createMention(User recipient, User sender, String pageTitle, Long pageId, String context) {
        return Notification.builder()
                .type(NotificationType.PAGE_MENTIONED)
                .title("멘션")
                .content(sender.getUsername() + "님이 '" + pageTitle + "' 페이지에서 당신을 멘션했습니다.")
                .recipient(recipient)
                .sender(sender)
                .referenceType("PAGE")
                .referenceId(pageId)
                .metadata("{\"context\":\"" + context + "\"}")
                .priority(Priority.HIGH)
                .build();
    }

    /**
     * 시스템 공지 알림 생성
     */
    public static Notification createSystemAnnouncement(User recipient, String title, String content) {
        return Notification.builder()
                .type(NotificationType.SYSTEM_ANNOUNCEMENT)
                .title(title)
                .content(content)
                .recipient(recipient)
                .sender(null) // 시스템 알림
                .priority(Priority.HIGH)
                .build();
    }

    /**
     * 보안 알림 생성
     */
    public static Notification createSecurityAlert(User recipient, String message) {
        return Notification.builder()
                .type(NotificationType.SECURITY_ALERT)
                .title("보안 알림")
                .content(message)
                .recipient(recipient)
                .sender(null)
                .priority(Priority.URGENT)
                .build();
    }
}