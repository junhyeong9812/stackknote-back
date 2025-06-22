package com.stacknote.back.domain.tag.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 페이지에 태그 추가 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TagAddToPageRequest {

    @NotNull(message = "페이지 ID는 필수입니다.")
    private Long pageId;

    @NotEmpty(message = "태그 이름 목록은 필수입니다.")
    private List<String> tagNames;
}