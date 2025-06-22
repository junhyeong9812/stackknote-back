package com.stacknote.back.domain.notification.repository;

import com.stacknote.back.domain.notification.entity.Notification;
import com.stacknote.back.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 알림 Repository
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 알림 ID로 활성 알림 조회
     */
    @Query("SELECT n FROM Notification n WHERE n.id = :id AND n.deletedAt IS NULL")
    Optional<Notification> findActiveNotificationById(@Param("id") Long id);

    /**
     * 사용자의 모든 알림 조회 (최신순)
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    List<Notification> findNotificationsByRecipient(@Param("recipient") User recipient, Pageable pageable);

    /**
     * 사용자의 읽지 않은 알림 조회
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.isRead = false AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    List<Notification> findUnreadNotificationsByRecipient(@Param("recipient") User recipient);

    /**
     * 사용자의 읽지 않은 알림 수 조회
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient = :recipient AND n.isRead = false AND n.deletedAt IS NULL")
    long countUnreadNotificationsByRecipient(@Param("recipient") User recipient);

    /**
     * 사용자의 높은 우선순위 읽지 않은 알림 조회
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.isRead = false AND n.priority IN ('HIGH', 'URGENT') AND n.deletedAt IS NULL ORDER BY n.priority DESC, n.createdAt DESC")
    List<Notification> findHighPriorityUnreadNotifications(@Param("recipient") User recipient);

    /**
     * 알림 타입별 조회
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.type = :type AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    List<Notification> findNotificationsByRecipientAndType(@Param("recipient") User recipient, @Param("type") Notification.NotificationType type, Pageable pageable);

    /**
     * 특정 기간 내 알림 조회
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.createdAt BETWEEN :startDate AND :endDate AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    List<Notification> findNotificationsByRecipientAndDateRange(@Param("recipient") User recipient, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 시스템 알림 조회
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.sender IS NULL AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    List<Notification> findSystemNotificationsByRecipient(@Param("recipient") User recipient, Pageable pageable);

    /**
     * 특정 참조에 대한 알림 조회
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.referenceType = :referenceType AND n.referenceId = :referenceId AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    List<Notification> findNotificationsByReference(@Param("recipient") User recipient, @Param("referenceType") String referenceType, @Param("referenceId") Long referenceId);

    /**
     * 발송자별 알림 조회
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.sender = :sender AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    List<Notification> findNotificationsBySender(@Param("recipient") User recipient, @Param("sender") User sender, Pageable pageable);

    /**
     * 중복 알림 확인 (같은 타입, 참조, 발송자)
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.type = :type AND n.referenceType = :referenceType AND n.referenceId = :referenceId AND n.sender = :sender AND n.createdAt > :since AND n.deletedAt IS NULL")
    List<Notification> findDuplicateNotifications(@Param("recipient") User recipient, @Param("type") Notification.NotificationType type, @Param("referenceType") String referenceType, @Param("referenceId") Long referenceId, @Param("sender") User sender, @Param("since") LocalDateTime since);

    /**
     * 긴급 알림 조회
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.priority = 'URGENT' AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    List<Notification> findUrgentNotificationsByRecipient(@Param("recipient") User recipient);

    /**
     * 만료된 알림 조회 (30일 이상 된 읽은 알림)
     */
    @Query("SELECT n FROM Notification n WHERE n.isRead = true AND n.createdAt < :expiryDate AND n.deletedAt IS NULL")
    List<Notification> findExpiredNotifications(@Param("expiryDate") LocalDateTime expiryDate, Pageable pageable);

    /**
     * 사용자의 최근 N개 알림 조회
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotificationsByRecipient(@Param("recipient") User recipient, Pageable pageable);

    /**
     * 알림 통계 조회
     */
    @Query("""
        SELECT 
            COUNT(n) as total,
            COUNT(CASE WHEN n.isRead = false THEN 1 END) as unread,
            COUNT(CASE WHEN n.priority = 'HIGH' OR n.priority = 'URGENT' THEN 1 END) as highPriority,
            COUNT(CASE WHEN n.sender IS NULL THEN 1 END) as systemNotifications
        FROM Notification n 
        WHERE n.recipient = :recipient AND n.deletedAt IS NULL
        """)
    Object[] getNotificationStatistics(@Param("recipient") User recipient);

    /**
     * 타입별 알림 수 조회
     */
    @Query("SELECT n.type, COUNT(n) FROM Notification n WHERE n.recipient = :recipient AND n.deletedAt IS NULL GROUP BY n.type")
    List<Object[]> getNotificationCountByType(@Param("recipient") User recipient);

    /**
     * 모든 읽지 않은 알림을 읽음 처리
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.recipient = :recipient AND n.isRead = false AND n.deletedAt IS NULL")
    int markAllAsReadByRecipient(@Param("recipient") User recipient);

    /**
     * 특정 타입의 모든 알림을 읽음 처리
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.recipient = :recipient AND n.type = :type AND n.isRead = false AND n.deletedAt IS NULL")
    int markAsReadByRecipientAndType(@Param("recipient") User recipient, @Param("type") Notification.NotificationType type);

    /**
     * 특정 참조에 대한 모든 알림을 읽음 처리
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.recipient = :recipient AND n.referenceType = :referenceType AND n.referenceId = :referenceId AND n.isRead = false AND n.deletedAt IS NULL")
    int markAsReadByReference(@Param("recipient") User recipient, @Param("referenceType") String referenceType, @Param("referenceId") Long referenceId);

    /**
     * 사용자의 모든 알림 소프트 삭제
     */
    @Modifying
    @Query("UPDATE Notification n SET n.deletedAt = CURRENT_TIMESTAMP WHERE n.recipient = :recipient AND n.deletedAt IS NULL")
    int softDeleteNotificationsByRecipient(@Param("recipient") User recipient);

    /**
     * 특정 타입의 알림 소프트 삭제
     */
    @Modifying
    @Query("UPDATE Notification n SET n.deletedAt = CURRENT_TIMESTAMP WHERE n.recipient = :recipient AND n.type = :type AND n.deletedAt IS NULL")
    int softDeleteNotificationsByType(@Param("recipient") User recipient, @Param("type") Notification.NotificationType type);

    /**
     * 만료된 알림 정리 (물리적 삭제)
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.createdAt < :expiryDate")
    int deleteExpiredNotifications(@Param("expiryDate") LocalDateTime expiryDate);

    /**
     * 읽은 알림 정리 (특정 개수 이상 유지)
     */
    @Query(value = """
        SELECT n.id FROM notifications n 
        WHERE n.recipient_id = :recipientId AND n.is_read = true AND n.deleted_at IS NULL
        ORDER BY n.read_at DESC 
        LIMIT :offset, :limit
        """, nativeQuery = true)
    List<Long> findOldReadNotificationIds(@Param("recipientId") Long recipientId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 특정 ID 목록의 알림 삭제
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.id IN :ids")
    int deleteNotificationsByIds(@Param("ids") List<Long> ids);

    /**
     * 발송자별 알림 수 조회
     */
    @Query("SELECT n.sender.username, COUNT(n) FROM Notification n WHERE n.recipient = :recipient AND n.sender IS NOT NULL AND n.deletedAt IS NULL GROUP BY n.sender.username ORDER BY COUNT(n) DESC")
    List<Object[]> getNotificationCountBySender(@Param("recipient") User recipient);

    /**
     * 최근 7일간의 알림 수 조회
     */
    @Query("SELECT DATE(n.createdAt), COUNT(n) FROM Notification n WHERE n.recipient = :recipient AND n.createdAt >= :since AND n.deletedAt IS NULL GROUP BY DATE(n.createdAt) ORDER BY DATE(n.createdAt)")
    List<Object[]> getNotificationCountByDate(@Param("recipient") User recipient, @Param("since") LocalDateTime since);
}