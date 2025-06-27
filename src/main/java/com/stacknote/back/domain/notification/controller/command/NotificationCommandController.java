package com.stacknote.back.domain.notification.controller.command;

import com.stacknote.back.domain.notification.dto.request.NotificationCreateRequest;
import com.stacknote.back.domain.notification.dto.request.NotificationUpdateRequest;
import com.stacknote.back.domain.notification.dto.response.NotificationResponse;
import com.stacknote.back.domain.notification.entity.Notification;
import com.stacknote.back.domain.notification.service.command.NotificationCommandService;
import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.domain.user.repository.UserRepository;
import com.stacknote.back.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 알림 명령 컨트롤러
 */
@Tag(name = "알림 관리", description = "알림 생성, 수정, 삭제 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationCommandController {

    private final NotificationCommandService notificationCommandService;
    private final UserRepository userRepository;
    @Operation(summary = "알림 생성", description = "새로운 알림을 생성합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NotificationResponse> createNotification(
            @Valid @RequestBody NotificationCreateRequest request) {

        NotificationResponse notification = notificationCommandService.createNotification(request);

        return ApiResponse.success("알림 생성 성공", notification);
    }

    @Operation(summary = "알림 수정", description = "알림 내용을 수정합니다.")
    @PutMapping("/{notificationId}")
    public ApiResponse<NotificationResponse> updateNotification(
            @Parameter(description = "알림 ID") @PathVariable Long notificationId,
            @Valid @RequestBody NotificationUpdateRequest request,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        NotificationResponse notification = notificationCommandService.updateNotification(notificationId, request, currentUser);

        return ApiResponse.success("알림 수정 성공", notification);
    }

    @Operation(summary = "알림 읽음 처리", description = "알림을 읽음으로 표시합니다.")
    @PostMapping("/{notificationId}/read")
    public ApiResponse<Void> markAsRead(
            @Parameter(description = "알림 ID") @PathVariable Long notificationId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        notificationCommandService.markAsRead(notificationId, currentUser);

        return ApiResponse.success("알림 읽음 처리 성공");
    }

    @Operation(summary = "알림 읽지 않음 처리", description = "알림을 읽지 않음으로 표시합니다.")
    @PostMapping("/{notificationId}/unread")
    public ApiResponse<Void> markAsUnread(
            @Parameter(description = "알림 ID") @PathVariable Long notificationId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        notificationCommandService.markAsUnread(notificationId, currentUser);

        return ApiResponse.success("알림 읽지 않음 처리 성공");
    }

    @Operation(summary = "모든 알림 읽음 처리", description = "현재 사용자의 모든 알림을 읽음으로 표시합니다.")
    @PostMapping("/my/read-all")
    public ApiResponse<Void> markAllAsRead(
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        notificationCommandService.markAllAsRead(currentUser);

        return ApiResponse.success("모든 알림 읽음 처리 성공");
    }

    @Operation(summary = "타입별 알림 읽음 처리", description = "특정 타입의 모든 알림을 읽음으로 표시합니다.")
    @PostMapping("/my/read-by-type/{type}")
    public ApiResponse<Void> markAsReadByType(
            @Parameter(description = "알림 타입") @PathVariable Notification.NotificationType type,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        notificationCommandService.markAsReadByType(currentUser, type);

        return ApiResponse.success("타입별 알림 읽음 처리 성공");
    }

    @Operation(summary = "참조별 알림 읽음 처리", description = "특정 참조에 대한 모든 알림을 읽음으로 표시합니다.")
    @PostMapping("/my/read-by-reference")
    public ApiResponse<Void> markAsReadByReference(
            @Parameter(description = "참조 타입") @RequestParam String referenceType,
            @Parameter(description = "참조 ID") @RequestParam Long referenceId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        notificationCommandService.markAsReadByReference(currentUser, referenceType, referenceId);

        return ApiResponse.success("참조별 알림 읽음 처리 성공");
    }

    @Operation(summary = "알림 삭제", description = "알림을 삭제합니다.")
    @DeleteMapping("/{notificationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteNotification(
            @Parameter(description = "알림 ID") @PathVariable Long notificationId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        notificationCommandService.deleteNotification(notificationId, currentUser);

        return ApiResponse.success("알림 삭제 성공");
    }

    @Operation(summary = "모든 알림 삭제", description = "현재 사용자의 모든 알림을 삭제합니다.")
    @DeleteMapping("/my/all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteAllNotifications(
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        notificationCommandService.deleteAllNotifications(currentUser);

        return ApiResponse.success("모든 알림 삭제 성공");
    }

    @Operation(summary = "타입별 알림 삭제", description = "특정 타입의 모든 알림을 삭제합니다.")
    @DeleteMapping("/my/type/{type}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteNotificationsByType(
            @Parameter(description = "알림 타입") @PathVariable Notification.NotificationType type,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        notificationCommandService.deleteNotificationsByType(currentUser, type);

        return ApiResponse.success("타입별 알림 삭제 성공");
    }

    // ===== 특수 알림 생성 메서드들 =====

    @Operation(summary = "워크스페이스 초대 알림 생성", description = "워크스페이스 초대 알림을 생성합니다.")
    @PostMapping("/workspace-invitation")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NotificationResponse> createWorkspaceInvitationNotification(
            @Parameter(description = "수신자 ID") @RequestParam Long recipientId,
            @Parameter(description = "워크스페이스 이름") @RequestParam String workspaceName,
            @Parameter(description = "워크스페이스 ID") @RequestParam Long workspaceId,
            Authentication authentication) {

        User sender = getCurrentUser(authentication);
        User recipient = getUserById(recipientId);

        NotificationResponse notification = notificationCommandService.createWorkspaceInvitationNotification(
                recipient, sender, workspaceName, workspaceId);

        return ApiResponse.success("워크스페이스 초대 알림 생성 성공", notification);
    }

    @Operation(summary = "페이지 댓글 알림 생성", description = "페이지 댓글 알림을 생성합니다.")
    @PostMapping("/page-comment")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NotificationResponse> createPageCommentNotification(
            @Parameter(description = "수신자 ID") @RequestParam Long recipientId,
            @Parameter(description = "페이지 제목") @RequestParam String pageTitle,
            @Parameter(description = "페이지 ID") @RequestParam Long pageId,
            @Parameter(description = "댓글 ID") @RequestParam Long commentId,
            Authentication authentication) {

        User sender = getCurrentUser(authentication);
        User recipient = getUserById(recipientId);

        NotificationResponse notification = notificationCommandService.createPageCommentNotification(
                recipient, sender, pageTitle, pageId, commentId);

        return ApiResponse.success("페이지 댓글 알림 생성 성공", notification);
    }

    @Operation(summary = "댓글 답글 알림 생성", description = "댓글 답글 알림을 생성합니다.")
    @PostMapping("/comment-reply")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NotificationResponse> createCommentReplyNotification(
            @Parameter(description = "수신자 ID") @RequestParam Long recipientId,
            @Parameter(description = "페이지 제목") @RequestParam String pageTitle,
            @Parameter(description = "댓글 ID") @RequestParam Long commentId,
            Authentication authentication) {

        User sender = getCurrentUser(authentication);
        User recipient = getUserById(recipientId);

        NotificationResponse notification = notificationCommandService.createCommentReplyNotification(
                recipient, sender, pageTitle, commentId);

        return ApiResponse.success("댓글 답글 알림 생성 성공", notification);
    }

    @Operation(summary = "멘션 알림 생성", description = "멘션 알림을 생성합니다.")
    @PostMapping("/mention")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NotificationResponse> createMentionNotification(
            @Parameter(description = "수신자 ID") @RequestParam Long recipientId,
            @Parameter(description = "페이지 제목") @RequestParam String pageTitle,
            @Parameter(description = "페이지 ID") @RequestParam Long pageId,
            @Parameter(description = "멘션 컨텍스트") @RequestParam String context,
            Authentication authentication) {

        User sender = getCurrentUser(authentication);
        User recipient = getUserById(recipientId);

        NotificationResponse notification = notificationCommandService.createMentionNotification(
                recipient, sender, pageTitle, pageId, context);

        return ApiResponse.success("멘션 알림 생성 성공", notification);
    }

    @Operation(summary = "시스템 공지 알림 생성", description = "시스템 공지 알림을 생성합니다.")
    @PostMapping("/system-announcement")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NotificationResponse> createSystemAnnouncementNotification(
            @Parameter(description = "수신자 ID") @RequestParam Long recipientId,
            @Parameter(description = "제목") @RequestParam String title,
            @Parameter(description = "내용") @RequestParam String content) {

        User recipient = getUserById(recipientId);

        NotificationResponse notification = notificationCommandService.createSystemAnnouncementNotification(
                recipient, title, content);

        return ApiResponse.success("시스템 공지 알림 생성 성공", notification);
    }

    @Operation(summary = "보안 알림 생성", description = "보안 알림을 생성합니다.")
    @PostMapping("/security-alert")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NotificationResponse> createSecurityAlertNotification(
            @Parameter(description = "수신자 ID") @RequestParam Long recipientId,
            @Parameter(description = "알림 메시지") @RequestParam String message) {

        User recipient = getUserById(recipientId);

        NotificationResponse notification = notificationCommandService.createSecurityAlertNotification(
                recipient, message);

        return ApiResponse.success("보안 알림 생성 성공", notification);
    }

    /**
     * 현재 사용자 조회
     */
    private User getCurrentUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    /**
     * 사용자 ID로 사용자 조회 (임시 구현)
     */
    private User getUserById(Long userId) {
        // 실제로는 UserRepository를 주입받아서 사용해야 함
        // 여기서는 간단히 구현

        User user = userRepository.findById(userId).orElseThrow();
        // user 설정...
        return user;
    }
}