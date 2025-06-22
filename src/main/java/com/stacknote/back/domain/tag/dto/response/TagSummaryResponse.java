package com.stacknote.back.domain.tag.dto.response;

import com.stacknote.back.domain.tag.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 태그 요약 응답 DTO (목록용)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagSummaryResponse {

    private Long id;
    private String name;
    private String color;
    private Integer usageCount;
    private Boolean isSystemTag;

    /**
     * Tag 엔티티로부터 TagSummaryResponse 생성
     */
    public static TagSummaryResponse from(Tag tag) {
        return TagSummaryResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .color(tag.getColor())
                .usageCount(tag.getUsageCount())
                .isSystemTag(tag.getIsSystemTag())
                .build();
    }
}