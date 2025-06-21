package com.stacknote.back.domain.file.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 파일 업로드 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadRequest {

    private Long pageId; // 연결할 페이지 ID (null이면 워크스페이스 전체 파일)

    @Size(max = 500, message = "파일 설명은 500자를 초과할 수 없습니다.")
    private String description;

    private Boolean isPublic = false; // 공개 파일 여부
}