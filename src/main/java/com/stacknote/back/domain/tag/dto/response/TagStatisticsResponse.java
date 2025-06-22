package com.stacknote.back.domain.tag.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 태그 통계 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
public class TagStatisticsResponse {

    private Long totalTags;        // 전체 태그 수
    private Long usedTags;         // 사용 중인 태그 수
    private Long systemTags;       // 시스템 태그 수
    private Double averageUsage;   // 평균 사용 횟수

    public TagStatisticsResponse(Long totalTags, Long usedTags, Long systemTags, Double averageUsage) {
        this.totalTags = totalTags;
        this.usedTags = usedTags;
        this.systemTags = systemTags;
        this.averageUsage = averageUsage;
    }
}