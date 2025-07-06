package com.stacknote.back.domain.page.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 페이지 통계 응답 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "페이지 통계 정보")
public class PageStatisticsResponse {

    @Schema(description = "전체 페이지 수")
    private long totalPages;

    @Schema(description = "공개된 페이지 수")
    private long publishedPages;

    @Schema(description = "초안 페이지 수")
    private long draftPages;

    @Schema(description = "템플릿 페이지 수")
    private long templatePages;

    @Schema(description = "전체 조회수")
    private long totalViews;

    @Schema(description = "평균 조회수")
    private double averageViews;

    @Schema(description = "가장 많이 조회된 페이지 ID")
    private Long mostViewedPageId;

    @Schema(description = "가장 많이 조회된 페이지 제목")
    private String mostViewedPageTitle;

    @Schema(description = "최근 7일간 생성된 페이지 수")
    private long recentlyCreatedCount;

    @Schema(description = "최근 7일간 수정된 페이지 수")
    private long recentlyModifiedCount;
}