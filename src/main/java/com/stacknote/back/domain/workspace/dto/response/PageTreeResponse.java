package com.stacknote.back.domain.workspace.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 페이지 트리 응답 DTO
 * 계층 구조의 페이지 정보를 표현
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "페이지 트리 구조")
public class PageTreeResponse {

    @Schema(description = "페이지 ID")
    private Long id;

    @Schema(description = "페이지 제목")
    private String title;

    @Schema(description = "페이지 아이콘", example = "📄")
    private String icon;

    @Schema(description = "부모 페이지 ID", nullable = true)
    private Long parentId;

    @Schema(description = "깊이 (0부터 시작)")
    private int depth;

    @Schema(description = "정렬 순서")
    private int sortOrder;

    @Schema(description = "자식 페이지 존재 여부")
    @Builder.Default
    private boolean hasChildren = false;

    @Schema(description = "공개 상태")
    @Builder.Default
    private boolean isPublished = false;

    @Schema(description = "잠금 상태")
    @Builder.Default
    private boolean isLocked = false;

    @Schema(description = "자식 페이지 목록")
    @Builder.Default
    private List<PageTreeResponse> children = new ArrayList<>();
}