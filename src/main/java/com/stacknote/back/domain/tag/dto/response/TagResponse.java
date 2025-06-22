package com.stacknote.back.domain.tag.dto.response;

import com.stacknote.back.domain.tag.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 태그 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagResponse {

    private Long id;
    private String name;
    private String color;
    private String description;
    private Long workspaceId;
    private String workspaceName;
    private Integer usageCount;
    private Boolean isSystemTag;
    private Boolean canDelete;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Tag 엔티티로부터 TagResponse 생성
     */
    public static TagResponse from(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .color(tag.getColor())
                .description(tag.getDescription())
                .workspaceId(tag.getWorkspace().getId())
                .workspaceName(tag.getWorkspace().getName())
                .usageCount(tag.getUsageCount())
                .isSystemTag(tag.getIsSystemTag())
                .canDelete(tag.canDelete())
                .createdAt(tag.getCreatedAt())
                .updatedAt(tag.getUpdatedAt())
                .build();
    }
}