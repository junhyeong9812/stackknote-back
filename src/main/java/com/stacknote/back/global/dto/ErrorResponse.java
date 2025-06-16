package com.stacknote.back.global.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 에러 응답 DTO
 * 에러 발생 시 상세 정보를 포함한 응답
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private String errorCode;         // 에러 코드
    private String message;           // 에러 메시지
    private List<FieldError> fieldErrors; // 필드 검증 에러 목록
    private LocalDateTime timestamp;  // 에러 발생 시각

    public ErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String errorCode, String message, List<FieldError> fieldErrors) {
        this.errorCode = errorCode;
        this.message = message;
        this.fieldErrors = fieldErrors;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 필드 검증 에러 정보
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        private String field;         // 에러가 발생한 필드명
        private Object rejectedValue; // 거부된 값
        private String message;       // 에러 메시지
    }
}