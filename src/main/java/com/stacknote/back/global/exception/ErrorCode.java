package com.stacknote.back.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 정의
 * 모든 예외에 대한 표준화된 에러 코드와 메시지 관리
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통 에러
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_001", "잘못된 요청입니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_002", "입력값이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_003", "지원하지 않는 HTTP 메서드입니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_004", "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_005", "서버 내부 오류가 발생했습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMMON_006", "접근 권한이 없습니다."),

    // 인증 관련 에러
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_003", "토큰이 만료되었습니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_004", "로그인에 실패했습니다."),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_005", "리프레시 토큰이 유효하지 않습니다."),

    // 사용자 관련 에러
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER_002", "이미 사용 중인 이메일입니다."),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "USER_003", "이미 사용 중인 사용자명입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "USER_004", "비밀번호가 올바르지 않습니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "USER_005", "비밀번호가 일치하지 않습니다."),
    USER_INACTIVE(HttpStatus.FORBIDDEN, "USER_006", "비활성화된 사용자입니다."),
    USER_DELETED(HttpStatus.FORBIDDEN, "USER_007", "삭제된 사용자입니다."),

    // 워크스페이스 관련 에러 (추후 확장)
    WORKSPACE_NOT_FOUND(HttpStatus.NOT_FOUND, "WORKSPACE_001", "워크스페이스를 찾을 수 없습니다."),
    WORKSPACE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "WORKSPACE_002", "워크스페이스 접근 권한이 없습니다."),

    // 페이지 관련 에러 (추후 확장)
    PAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "PAGE_001", "페이지를 찾을 수 없습니다."),
    PAGE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PAGE_002", "페이지 접근 권한이 없습니다."),

    // 파일 관련 에러 (추후 확장)
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE_001", "파일을 찾을 수 없습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_002", "파일 업로드에 실패했습니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "FILE_003", "파일 크기가 제한을 초과했습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "FILE_004", "지원하지 않는 파일 형식입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}