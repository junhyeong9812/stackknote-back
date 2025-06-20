package com.stacknote.back.domain.page.dto.response;

import com.stacknote.back.domain.page.entity.Page;
import com.stacknote.back.domain.user.dto.response.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 페이지 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse {

    private Long id;
    private String title;
    private String content;
    private String summary;
    private String icon;
    private String coverImageUrl;
    private Long workspaceId;
    private String workspaceName;
    private Long parentId;
    private String parentTitle;
    private UserResponse createdBy;
    private UserResponse lastModifiedBy;
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
     * Page 엔티티로부터 PageResponse 생성
     */
    public static PageResponse from(Page page) {
        return PageResponse.builder()
                .id(page.getId())
                .title(page.getTitle())
                .content(page.getContent())
                .summary(page.getSummary())
                .icon(page.getIcon())
                .coverImageUrl(page.getCoverImageUrl())
                .workspaceId(page.getWorkspace().getId())
                .workspaceName(page.getWorkspace().getName())
                .parentId(page.getParent() != null ? page.getParent().getId() : null)
                .parentTitle(page.getParent() != null ? page.getParent().getTitle() : null)
                .createdBy(UserResponse.from(page.getCreatedBy()))
                .lastModifiedBy(page.getLastModifiedBy() != null ?
                        UserResponse.from(page.getLastModifiedBy()) : null)
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

    /**
     * 콘텐츠 없는 요약 버전 생성
     */
    public static PageResponse fromWithoutContent(Page page) {
        PageResponse response = from(page);
        return PageResponse.builder()
                .id(response.id)
                .title(response.title)
                .summary(response.summary)
                .icon(response.icon)
                .coverImageUrl(response.coverImageUrl)
                .workspaceId(response.workspaceId)
                .workspaceName(response.workspaceName)
                .parentId(response.parentId)
                .parentTitle(response.parentTitle)
                .createdBy(response.createdBy)
                .lastModifiedBy(response.lastModifiedBy)
                .isPublished(response.isPublished)
                .isTemplate(response.isTemplate)
                .isLocked(response.isLocked)
                .sortOrder(response.sortOrder)
                .viewCount(response.viewCount)
                .pageType(response.pageType)
                .depth(response.depth)
                .hasChildren(response.hasChildren)
                .createdAt(response.createdAt)
                .updatedAt(response.updatedAt)
                .build();
    }
}