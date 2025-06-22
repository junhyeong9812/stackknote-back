package com.stacknote.back.domain.notification.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 알림 수정 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationUpdateRequest {

    @Size(max = 255, message = "알림 제목은 255자를 초과할 수 없습니다.")
    private String title;

    @Size(max = 1000, message = "알림 내용은 1000자를 초과할 수 없습니다.")
    private String content;

    @Size(max = 500, message = "액션 URL은 500자를 초과할 수 없습니다.")
    private String actionUrl;

    @Size(max = 1000, message = "메타데이터는 1000자를 초과할 수 없습니다.")
    private String metadata;
}