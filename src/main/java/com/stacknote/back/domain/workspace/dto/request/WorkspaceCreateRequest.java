package com.stacknote.back.domain.workspace.dto.request;

import com.stacknote.back.domain.workspace.entity.Workspace;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 워크스페이스 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceCreateRequest {

    @NotBlank(message = "워크스페이스 이름은 필수입니다.")
    @Size(min = 1, max = 100, message = "워크스페이스 이름은 1자 이상 100자 이하여야 합니다.")
    private String name;

    @Size(max = 500, message = "설명은 500자를 초과할 수 없습니다.")
    private String description;

    @Size(max = 10, message = "아이콘은 10자를 초과할 수 없습니다.")
    @Pattern(regexp = "^[\\p{So}\\p{Sk}]*$", message = "아이콘은 이모지만 입력 가능합니다.")
    private String icon;

    @Size(max = 500, message = "커버 이미지 URL은 500자를 초과할 수 없습니다.")
    private String coverImageUrl;

    private Workspace.Visibility visibility = Workspace.Visibility.PRIVATE;
}