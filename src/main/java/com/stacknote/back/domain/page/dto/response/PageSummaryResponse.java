package com.stacknote.back.domain.page.dto.response;

import com.stacknote.back.domain.page.entity.Page;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 페이지 요약 응답 DTO (목록용)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageSummaryResponse {

    private Long id;
    private String title;
    private String summary;
    private String icon;
    private String coverImageUrl;
    private Long parentId;
    private String createdByName;
    private String lastModifiedByName;
    private Boolean isPublished;
    private Boolean isTemplate;
    private Boolean isLocked;
    private Integer sortOrder;
    private Long viewCount;
    private String pageType;
    private Integer depth;
    private Boolean hasChildren;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Page 엔티티로부터 PageSummaryResponse 생성
     */
    public static PageSummaryResponse from(Page page) {
        return PageSummaryResponse.builder()
                .id(page.getId())
                .title(page.getTitle())
                .summary(page.getSummary())
                .icon(page.getIcon())
                .coverImageUrl(page.getCoverImageUrl())
                .parentId(page.getParent() != null ? page.getParent().getId() : null)
                .createdByName(page.getCreatedBy().getUsername())
                .lastModifiedByName(page.getLastModifiedBy() != null ?
                        page.getLastModifiedBy().getUsername() : null)
                .isPublished(page.getIsPublished())
                .isTemplate(page.getIsTemplate())
                .isLocked(page.getIsLocked())
                .sortOrder(page.getSortOrder())
                .viewCount(page.getViewCount())
                .pageType(page.getPageType().name())
                .depth(page.getDepth())
                .hasChildren(!page.getChildren().isEmpty())
                .createdAt(page.getCreatedAt())
                .updatedAt(page.getUpdatedAt())
                .build();
    }
}