package com.stacknote.back.domain.notification.controller.query;

import com.stacknote.back.domain.notification.dto.response.NotificationResponse;
import com.stacknote.back.domain.notification.dto.response.NotificationStatisticsResponse;
import com.stacknote.back.domain.notification.entity.Notification;
import com.stacknote.back.domain.notification.service.query.NotificationQueryService;
import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.domain.user.repository.UserRepository;
import com.stacknote.back.global.dto.ApiResponse;
import com.stacknote.back.global.exception.custom.EntityNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 알림 조회 컨트롤러
 */
@Tag(name = "알림 조회", description = "알림 조회 관련 API")
@Slf4j
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationQueryController {

    private final NotificationQueryService notificationQueryService;
    private final UserRepository userRepository;

    @Operation(summary = "알림 상세 조회", description = "알림 ID로 알림 상세 정보를 조회합니다.")
    @GetMapping("/{notificationId}")
    public ApiResponse<NotificationResponse> getNotification(
            @Parameter(description = "알림 ID") @PathVariable Long notificationId) {

        NotificationResponse notification = notificationQueryService.getNotificationById(notificationId);

        return ApiResponse.success("알림 조회 성공", notification);
    }

    @Operation(summary = "내 알림 목록 조회", description = "현재 사용자의 알림 목록을 조회합니다.")
    @GetMapping("/my")
    public ApiResponse<List<NotificationResponse>> getMyNotifications(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        List<NotificationResponse> notifications = notificationQueryService.getNotificationsByRecipient(currentUser, page, size);

        return ApiResponse.success("내 알림 목록 조회 성공", notifications);
    }

    @Operation(summary = "읽지 않은 알림 조회", description = "현재 사용자의 읽지 않은 알림을 조회합니다.")
    @GetMapping("/my/unread")
    public ApiResponse<List<NotificationResponse>> getUnreadNotifications(
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        List<NotificationResponse> notifications = notificationQueryService.getUnreadNotificationsByRecipient(currentUser);

        return ApiResponse.success("읽지 않은 알림 조회 성공", notifications);
    }

    @Operation(summary = "읽지 않은 알림 수 조회", description = "현재 사용자의 읽지 않은 알림 수를 조회합니다.")
    @GetMapping("/my/unread/count")
    public ApiResponse<Long> getUnreadNotificationCount(
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        long count = notificationQueryService.getUnreadNotificationCount(currentUser);

        return ApiResponse.success("읽지 않은 알림 수 조회 성공", count);
    }

    @Operation(summary = "높은 우선순위 알림 조회", description = "현재 사용자의 높은 우선순위 읽지 않은 알림을 조회합니다.")
    @GetMapping("/my/high-priority")
    public ApiResponse<List<NotificationResponse>> getHighPriorityNotifications(
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        List<NotificationResponse> notifications = notificationQueryService.getHighPriorityUnreadNotifications(currentUser);

        return ApiResponse.success("높은 우선순위 알림 조회 성공", notifications);
    }

    @Operation(summary = "타입별 알림 조회", description = "특정 타입의 알림을 조회합니다.")
    @GetMapping("/my/type/{type}")
    public ApiResponse<List<NotificationResponse>> getNotificationsByType(
            @Parameter(description = "알림 타입") @PathVariable Notification.NotificationType type,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        List<NotificationResponse> notifications = notificationQueryService.getNotificationsByType(currentUser, type, page, size);

        return ApiResponse.success("타입별 알림 조회 성공", notifications);
    }

    @Operation(summary = "기간별 알림 조회", description = "특정 기간 내의 알림을 조회합니다.")
    @GetMapping("/my/date-range")
    public ApiResponse<List<NotificationResponse>> getNotificationsByDateRange(
            @Parameter(description = "시작 날짜") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "종료 날짜") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        List<NotificationResponse> notifications = notificationQueryService.getNotificationsByDateRange(currentUser, startDate, endDate);

        return ApiResponse.success("기간별 알림 조회 성공", notifications);
    }

    @Operation(summary = "시스템 알림 조회", description = "현재 사용자의 시스템 알림을 조회합니다.")
    @GetMapping("/my/system")
    public ApiResponse<List<NotificationResponse>> getSystemNotifications(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        List<NotificationResponse> notifications = notificationQueryService.getSystemNotificationsByRecipient(currentUser, page, size);

        return ApiResponse.success("시스템 알림 조회 성공", notifications);
    }

    @Operation(summary = "참조별 알림 조회", description = "특정 참조와 관련된 알림을 조회합니다.")
    @GetMapping("/my/reference")
    public ApiResponse<List<NotificationResponse>> getNotificationsByReference(
            @Parameter(description = "참조 타입") @RequestParam String referenceType,
            @Parameter(description = "참조 ID") @RequestParam Long referenceId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        List<NotificationResponse> notifications = notificationQueryService.getNotificationsByReference(currentUser, referenceType, referenceId);

        return ApiResponse.success("참조별 알림 조회 성공", notifications);
    }

    @Operation(summary = "발송자별 알림 조회", description = "특정 발송자의 알림을 조회합니다.")
    @GetMapping("/my/sender/{senderId}")
    public ApiResponse<List<NotificationResponse>> getNotificationsBySender(
            @Parameter(description = "발송자 ID") @PathVariable Long senderId,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new EntityNotFoundException("발송자를 찾을 수 없습니다."));

        List<NotificationResponse> notifications = notificationQueryService.getNotificationsBySender(currentUser, sender, page, size);

        return ApiResponse.success("발송자별 알림 조회 성공", notifications);
    }

    @Operation(summary = "긴급 알림 조회", description = "현재 사용자의 긴급 알림을 조회합니다.")
    @GetMapping("/my/urgent")
    public ApiResponse<List<NotificationResponse>> getUrgentNotifications(
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        List<NotificationResponse> notifications = notificationQueryService.getUrgentNotificationsByRecipient(currentUser);

        return ApiResponse.success("긴급 알림 조회 성공", notifications);
    }

    @Operation(summary = "최근 알림 조회", description = "현재 사용자의 최근 알림을 조회합니다.")
    @GetMapping("/my/recent")
    public ApiResponse<List<NotificationResponse>> getRecentNotifications(
            @Parameter(description = "조회할 알림 수") @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        List<NotificationResponse> notifications = notificationQueryService.getRecentNotificationsByRecipient(currentUser, size);

        return ApiResponse.success("최근 알림 조회 성공", notifications);
    }

    @Operation(summary = "알림 통계 조회", description = "현재 사용자의 알림 통계를 조회합니다.")
    @GetMapping("/my/statistics")
    public ApiResponse<NotificationStatisticsResponse> getNotificationStatistics(
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        NotificationStatisticsResponse statistics = notificationQueryService.getNotificationStatistics(currentUser);

        return ApiResponse.success("알림 통계 조회 성공", statistics);
    }

    /**
     * 현재 사용자 조회
     */
    private User getCurrentUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }
}