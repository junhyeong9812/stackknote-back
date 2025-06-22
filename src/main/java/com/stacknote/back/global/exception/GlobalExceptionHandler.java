package com.stacknote.back.global.exception;

import com.stacknote.back.domain.comment.exception.CommentAccessDeniedException;
import com.stacknote.back.domain.comment.exception.CommentNotFoundException;
import com.stacknote.back.domain.comment.exception.InvalidCommentException;
import com.stacknote.back.domain.notification.exception.NotificationAccessDeniedException;
import com.stacknote.back.domain.notification.exception.NotificationNotFoundException;
import com.stacknote.back.domain.tag.exception.DuplicateTagException;
import com.stacknote.back.domain.tag.exception.TagInUseException;
import com.stacknote.back.domain.tag.exception.TagNotFoundException;
import com.stacknote.back.domain.user.exception.DuplicateEmailException;
import com.stacknote.back.domain.user.exception.InvalidPasswordException;
import com.stacknote.back.domain.user.exception.UserNotFoundException;
import com.stacknote.back.global.dto.ApiResponse;
import com.stacknote.back.global.dto.ErrorResponse;
import com.stacknote.back.global.exception.custom.BusinessException;
import com.stacknote.back.global.exception.custom.EntityNotFoundException;
import com.stacknote.back.global.exception.custom.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리 핸들러
 * 모든 예외를 일관된 형식으로 응답
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleBusinessException(BusinessException e) {
        log.warn("Business exception occurred: {}", e.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                e.getErrorCode().getCode(),
                e.getMessage()
        );

        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(ApiResponse.error(e.getMessage(), errorResponse));
    }

    /**
     * 사용자 도메인 예외 처리
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleUserNotFoundException(UserNotFoundException e) {
        log.warn("User not found: {}", e.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.USER_NOT_FOUND.getCode(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage(), errorResponse));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleDuplicateEmailException(DuplicateEmailException e) {
        log.warn("Duplicate email/username: {}", e.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.DUPLICATE_EMAIL.getCode(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(e.getMessage(), errorResponse));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleInvalidPasswordException(InvalidPasswordException e) {
        log.warn("Invalid password: {}", e.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.INVALID_PASSWORD.getCode(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage(), errorResponse));
    }

    /**
     * 댓글 도메인 예외 처리
     */
    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleCommentNotFoundException(CommentNotFoundException e) {
        log.warn("Comment not found: {}", e.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.COMMENT_NOT_FOUND.getCode(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage(), errorResponse));
    }

    @ExceptionHandler(CommentAccessDeniedException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleCommentAccessDeniedException(CommentAccessDeniedException e) {
        log.warn("Comment access denied: {}", e.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.COMMENT_ACCESS_DENIED.getCode(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(e.getMessage(), errorResponse));
    }

    @ExceptionHandler(InvalidCommentException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleInvalidCommentException(InvalidCommentException e) {
        log.warn("Invalid comment: {}", e.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.INVALID_COMMENT_CONTENT.getCode(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage(), errorResponse));
    }

    /**
     * 태그 도메인 예외 처리
     */
    @ExceptionHandler(TagNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleTagNotFoundException(TagNotFoundException e) {
        log.warn("Tag not found: {}", e.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.TAG_NOT_FOUND.getCode(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage(), errorResponse));
    }

    @ExceptionHandler(DuplicateTagException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleDuplicateTagException(DuplicateTagException e) {
        log.warn("Duplicate tag name: {}", e.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.DUPLICATE_TAG_NAME.getCode(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(e.getMessage(), errorResponse));
    }

    @ExceptionHandler(TagInUseException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleTagInUseException(TagInUseException e) {
        log.warn("Tag in use: {}", e.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.TAG_IN_USE.getCode(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage(), errorResponse));
    }

    /**
     * 알림 도메인 예외 처리
     */
    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleNotificationNotFoundException(NotificationNotFoundException e) {
        log.warn("Notification not found: {}", e.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.NOTIFICATION_NOT_FOUND.getCode(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage(), errorResponse));
    }

    @ExceptionHandler(NotificationAccessDeniedException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleNotificationAccessDeniedException(NotificationAccessDeniedException e) {
        log.warn("Notification access denied: {}", e.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.NOTIFICATION_ACCESS_DENIED.getCode(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(e.getMessage(), errorResponse));
    }

    /**
     * 인증/인가 예외 처리
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleUnauthorizedException(UnauthorizedException e) {
        log.warn("Unauthorized access: {}", e.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                e.getErrorCode().getCode(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(e.getMessage(), errorResponse));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.ACCESS_DENIED.getCode(),
                "접근 권한이 없습니다."
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("접근 권한이 없습니다.", errorResponse));
    }

    /**
     * 입력값 검증 예외 처리
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResponse<ErrorResponse>> handleValidationException(BindException e) {
        log.warn("Validation error: {}", e.getMessage());

        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorResponse.FieldError(
                        error.getField(),
                        error.getRejectedValue(),
                        error.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.INVALID_INPUT_VALUE.getCode(),
                "입력값 검증에 실패했습니다.",
                fieldErrors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("입력값 검증에 실패했습니다.", errorResponse));
    }

    /**
     * HTTP 메서드 지원하지 않음 예외 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("Method not supported: {}", e.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.METHOD_NOT_ALLOWED.getCode(),
                "지원하지 않는 HTTP 메서드입니다."
        );

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error("지원하지 않는 HTTP 메서드입니다.", errorResponse));
    }

    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Illegal argument: {}", e.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.INVALID_INPUT_VALUE.getCode(),
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage(), errorResponse));
    }

    /**
     * 기타 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleException(Exception e) {
        log.error("Unexpected error occurred", e);

        ErrorResponse errorResponse = new ErrorResponse(
                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                "서버 내부 오류가 발생했습니다."
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("서버 내부 오류가 발생했습니다.", errorResponse));
    }
}