package com.stacknote.back.domain.file.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 파일 정보 수정 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FileUpdateRequest {

    @Size(max = 500, message = "파일 설명은 500자를 초과할 수 없습니다.")
    private String description;

    private Boolean isPublic;

    private Long pageId; // 연결할 페이지 ID 변경
}