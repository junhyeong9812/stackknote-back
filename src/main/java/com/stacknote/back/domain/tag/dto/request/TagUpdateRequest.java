package com.stacknote.back.domain.tag.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 태그 수정 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TagUpdateRequest {

    @Size(min = 1, max = 50, message = "태그 이름은 1자 이상 50자 이하여야 합니다.")
    @Pattern(regexp = "^[^,]*$", message = "태그 이름에는 쉼표(,)를 사용할 수 없습니다.")
    private String name;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "색상은 #RRGGBB 형식이어야 합니다.")
    private String color;

    @Size(max = 255, message = "태그 설명은 255자를 초과할 수 없습니다.")
    private String description;
}