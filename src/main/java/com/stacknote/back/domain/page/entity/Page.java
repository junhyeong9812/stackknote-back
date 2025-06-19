package com.stacknote.back.domain.page.entity;

import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.domain.workspace.entity.Workspace;
import com.stacknote.back.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 페이지 엔티티
 * 노션 스타일의 문서/페이지를 나타내는 핵심 엔티티
 */
@Entity
@Table(name = "pages", indexes = {
        @Index(name = "idx_page_workspace", columnList = "workspace_id"),
        @Index(name = "idx_page_parent", columnList = "parent_id"),
        @Index(name = "idx_page_creator", columnList = "created_by"),
        @Index(name = "idx_page_title", columnList = "title"),
        @Index(name = "idx_page_is_published", columnList = "is_published")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Page extends BaseEntity {

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content; // 마크다운 형식

    @Column(name = "summary", length = 500)
    private String summary; // 자동 생성되는 요약

    @Column(name = "icon", length = 10)
    private String icon; // 이모지 또는 아이콘

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Page parent; // 부모 페이지 (계층 구조)

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Page> children = new ArrayList<>(); // 자식 페이지들

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy; // 페이지 생성자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_modified_by")
    private User lastModifiedBy; // 마지막 수정자

    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private Boolean isPublished = false; // 공개 여부

    @Column(name = "is_template", nullable = false)
    @Builder.Default
    private Boolean isTemplate = false; // 템플릿 여부

    @Column(name = "is_locked", nullable = false)
    @Builder.Default
    private Boolean isLocked = false; // 편집 잠금 여부

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0; // 정렬 순서

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Long viewCount = 0L; // 조회수

    @Enumerated(EnumType.STRING)
    @Column(name = "page_type", nullable = false)
    @Builder.Default
    private PageType pageType = PageType.DOCUMENT;

    // ===== 비즈니스 로직 메서드 =====

    /**
     * 페이지 정보 업데이트
     */
    public void updateInfo(String title, String content, String icon, String coverImageUrl) {
        if (title != null && !title.trim().isEmpty()) {
            this.title = title.trim();
        }
        if (content != null) {
            this.content = content;
            // 콘텐츠가 변경되면 요약도 업데이트
            this.summary = generateSummary(content);
        }
        if (icon != null) {
            this.icon = icon.trim().isEmpty() ? null : icon.trim();
        }
        if (coverImageUrl != null) {
            this.coverImageUrl = coverImageUrl.trim().isEmpty() ? null : coverImageUrl.trim();
        }
    }

    /**
     * 마지막 수정자 업데이트
     */
    public void updateLastModifiedBy(User user) {
        this.lastModifiedBy = user;
    }

    /**
     * 공개 상태 변경
     */
    public void publish() {
        this.isPublished = true;
    }

    /**
     * 비공개 상태 변경
     */
    public void unpublish() {
        this.isPublished = false;
    }

    /**
     * 템플릿으로 설정
     */
    public void markAsTemplate() {
        this.isTemplate = true;
    }

    /**
     * 템플릿 해제
     */
    public void unmarkAsTemplate() {
        this.isTemplate = false;
    }

    /**
     * 페이지 잠금
     */
    public void lock() {
        this.isLocked = true;
    }

    /**
     * 페이지 잠금 해제
     */
    public void unlock() {
        this.isLocked = false;
    }

    /**
     * 정렬 순서 변경
     */
    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * 조회수 증가
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 부모 페이지 설정
     */
    public void setParent(Page parent) {
        if (this.parent != null) {
            this.parent.children.remove(this);
        }
        this.parent = parent;
        if (parent != null) {
            parent.children.add(this);
        }
    }

    /**
     * 자식 페이지 추가
     */
    public void addChild(Page child) {
        child.setParent(this);
    }

    /**
     * 자식 페이지 제거
     */
    public void removeChild(Page child) {
        child.setParent(null);
    }

    /**
     * 최상위 페이지 여부 확인
     */
    public boolean isRootPage() {
        return this.parent == null;
    }

    /**
     * 페이지 깊이 계산
     */
    public int getDepth() {
        int depth = 0;
        Page current = this.parent;
        while (current != null) {
            depth++;
            current = current.parent;
        }
        return depth;
    }

    /**
     * 편집 가능 여부 확인
     */
    public boolean isEditable() {
        return !isLocked && !isDeleted();
    }

    /**
     * 페이지 타입 변경
     */
    public void changePageType(PageType pageType) {
        this.pageType = pageType;
    }

    /**
     * 콘텐츠에서 요약 생성
     */
    private String generateSummary(String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }

        // 마크다운 제거하고 첫 200자만 가져오기
        String plainText = content.replaceAll("[#*`>\\-+]", "").trim();
        if (plainText.length() > 200) {
            return plainText.substring(0, 200) + "...";
        }
        return plainText;
    }

    /**
     * 페이지 타입
     */
    public enum PageType {
        DOCUMENT,    // 일반 문서
        DATABASE,    // 데이터베이스 페이지
        KANBAN,      // 칸반 보드
        CALENDAR,    // 캘린더
        GALLERY,     // 갤러리
        LIST,        // 리스트
        TEMPLATE     // 템플릿
    }
}