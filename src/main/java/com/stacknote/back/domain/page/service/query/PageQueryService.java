package com.stacknote.back.domain.page.service.query;

import com.stacknote.back.domain.page.dto.response.PageResponse;
import com.stacknote.back.domain.page.dto.response.PageSummaryResponse;
import com.stacknote.back.domain.page.entity.Page;
import com.stacknote.back.domain.page.entity.PageHistory;
import com.stacknote.back.domain.page.exception.PageAccessDeniedException;
import com.stacknote.back.domain.page.exception.PageNotFoundException;
import com.stacknote.back.domain.page.repository.PageHistoryRepository;
import com.stacknote.back.domain.page.repository.PageRepository;
import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.domain.workspace.entity.Workspace;
import com.stacknote.back.domain.workspace.exception.WorkspaceNotFoundException;
import com.stacknote.back.domain.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 페이지 관련 쿼리 서비스
 * 페이지 조회, 검색 등의 읽기 전용 작업 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PageQueryService {

    private final PageRepository pageRepository;
    private final PageHistoryRepository pageHistoryRepository;
    private final WorkspaceRepository workspaceRepository;

    /**
     * 페이지 상세 조회
     */
    @Transactional // 조회수 증가를 위해 쓰기 트랜잭션 필요
    public PageResponse getPage(Long pageId, User currentUser) {
        log.debug("페이지 상세 조회: {}, 사용자: {}", pageId, currentUser.getId());

        Page page = getAccessiblePage(pageId, currentUser);

        // 조회수 증가
        pageRepository.incrementViewCount(pageId);

        return PageResponse.from(page);
    }

    /**
     * 워크스페이스의 페이지 목록 조회 (계층 구조)
     */
    public List<PageSummaryResponse> getWorkspacePages(Long workspaceId, User currentUser) {
        log.debug("워크스페이스 페이지 목록 조회: {}, 사용자: {}", workspaceId, currentUser.getId());

        Workspace workspace = getAccessibleWorkspace(workspaceId, currentUser);
        List<Page> pages = pageRepository.findByWorkspace(workspace);

        return pages.stream()
                .map(PageSummaryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 워크스페이스의 최상위 페이지들 조회
     */
    public List<PageSummaryResponse> getRootPages(Long workspaceId, User currentUser) {
        log.debug("최상위 페이지 목록 조회: {}, 사용자: {}", workspaceId, currentUser.getId());

        Workspace workspace = getAccessibleWorkspace(workspaceId, currentUser);
        List<Page> rootPages = pageRepository.findRootPagesByWorkspace(workspace);

        return rootPages.stream()
                .map(PageSummaryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 페이지의 자식 페이지들 조회
     */
    public List<PageSummaryResponse> getChildPages(Long pageId, User currentUser) {
        log.debug("자식 페이지 목록 조회: {}, 사용자: {}", pageId, currentUser.getId());

        Page page = getAccessiblePage(pageId, currentUser);
        List<Page> childPages = pageRepository.findByParent(page);

        return childPages.stream()
                .map(PageSummaryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 워크스페이스 내 페이지 검색
     */
    public List<PageSummaryResponse> searchPages(Long workspaceId, String keyword, User currentUser) {
        log.debug("페이지 검색: 워크스페이스: {}, 키워드: {}, 사용자: {}", workspaceId, keyword, currentUser.getId());

        Workspace workspace = getAccessibleWorkspace(workspaceId, currentUser);
        List<Page> pages = pageRepository.searchByContentInWorkspace(workspace, keyword);

        return pages.stream()
                .map(PageSummaryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 최근 수정된 페이지 목록 조회
     */
    public List<PageSummaryResponse> getRecentlyModifiedPages(Long workspaceId, User currentUser, int days, int limit) {
        log.debug("최근 수정된 페이지 목록 조회: 워크스페이스: {}, 사용자: {}", workspaceId, currentUser.getId());

        Workspace workspace = getAccessibleWorkspace(workspaceId, currentUser);
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        Pageable pageable = PageRequest.of(0, limit);

        List<Page> pages = pageRepository.findRecentlyModifiedInWorkspace(workspace, since, pageable);

        return pages.stream()
                .map(PageSummaryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 공개된 페이지 목록 조회
     */
    public List<PageSummaryResponse> getPublishedPages(Long workspaceId, User currentUser) {
        log.debug("공개된 페이지 목록 조회: 워크스페이스: {}, 사용자: {}", workspaceId, currentUser.getId());

        Workspace workspace = getAccessibleWorkspace(workspaceId, currentUser);
        List<Page> pages = pageRepository.findPublishedPagesByWorkspace(workspace);

        return pages.stream()
                .map(PageSummaryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 템플릿 페이지 목록 조회
     */
    public List<PageSummaryResponse> getTemplatePages(Long workspaceId, User currentUser) {
        log.debug("템플릿 페이지 목록 조회: 워크스페이스: {}, 사용자: {}", workspaceId, currentUser.getId());

        Workspace workspace = getAccessibleWorkspace(workspaceId, currentUser);
        List<Page> pages = pageRepository.findTemplatesByWorkspace(workspace);

        return pages.stream()
                .map(PageSummaryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 페이지 타입별 조회
     */
    public List<PageSummaryResponse> getPagesByType(Long workspaceId, Page.PageType pageType, User currentUser) {
        log.debug("페이지 타입별 조회: 워크스페이스: {}, 타입: {}, 사용자: {}", workspaceId, pageType, currentUser.getId());

        Workspace workspace = getAccessibleWorkspace(workspaceId, currentUser);
        List<Page> pages = pageRepository.findByWorkspaceAndPageType(workspace, pageType);

        return pages.stream()
                .map(PageSummaryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 인기 페이지 목록 조회 (조회수 기준)
     */
    public List<PageSummaryResponse> getPopularPages(Long workspaceId, User currentUser, int limit) {
        log.debug("인기 페이지 목록 조회: 워크스페이스: {}, 사용자: {}", workspaceId, currentUser.getId());

        Workspace workspace = getAccessibleWorkspace(workspaceId, currentUser);
        Pageable pageable = PageRequest.of(0, limit);
        List<Page> pages = pageRepository.findPopularPagesByWorkspace(workspace, pageable);

        return pages.stream()
                .map(PageSummaryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자가 생성한 페이지 목록 조회
     */
    public List<PageSummaryResponse> getUserCreatedPages(User user, int limit) {
        log.debug("사용자 생성 페이지 목록 조회: {}", user.getId());

        Pageable pageable = PageRequest.of(0, limit);
        List<Page> pages = pageRepository.findByCreatedBy(user);

        return pages.stream()
                .limit(limit)
                .map(PageSummaryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자가 최근 수정한 페이지 목록 조회
     */
    public List<PageSummaryResponse> getUserRecentlyModifiedPages(User user, int limit) {
        log.debug("사용자 최근 수정 페이지 목록 조회: {}", user.getId());

        Pageable pageable = PageRequest.of(0, limit);
        List<Page> pages = pageRepository.findRecentlyModifiedByUser(user, pageable);

        return pages.stream()
                .map(PageSummaryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 페이지 히스토리 조회
     */
    public List<PageHistoryResponse> getPageHistory(Long pageId, User currentUser, int limit) {
        log.debug("페이지 히스토리 조회: {}, 사용자: {}", pageId, currentUser.getId());

        Page page = getAccessiblePage(pageId, currentUser);
        Pageable pageable = PageRequest.of(0, limit);
        List<PageHistory> histories = pageHistoryRepository.findByPage(page, pageable);

        return histories.stream()
                .map(PageHistoryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 버전의 페이지 히스토리 조회
     */
    public PageHistoryResponse getPageHistoryVersion(Long pageId, Integer version, User currentUser) {
        log.debug("페이지 히스토리 버전 조회: {}, 버전: {}, 사용자: {}", pageId, version, currentUser.getId());

        Page page = getAccessiblePage(pageId, currentUser);
        PageHistory history = pageHistoryRepository.findByPageAndVersion(page, version)
                .orElseThrow(() -> new IllegalArgumentException("해당 버전을 찾을 수 없습니다."));

        return PageHistoryResponse.from(history);
    }

    /**
     * 워크스페이스 페이지 통계 조회
     */
    public PageStatisticsResponse getWorkspacePageStatistics(Long workspaceId, User currentUser) {
        log.debug("워크스페이스 페이지 통계 조회: {}, 사용자: {}", workspaceId, currentUser.getId());

        Workspace workspace = getAccessibleWorkspace(workspaceId, currentUser);

        long totalPages = pageRepository.countByWorkspace(workspace);
        long publishedPages = pageRepository.findPublishedPagesByWorkspace(workspace).size();
        long templatePages = pageRepository.findTemplatesByWorkspace(workspace).size();
        long lockedPages = pageRepository.findLockedPagesByWorkspace(workspace).size();

        return new PageStatisticsResponse(totalPages, publishedPages, templatePages, lockedPages);
    }

    // ===== 내부 헬퍼 메서드 =====

    private Workspace getAccessibleWorkspace(Long workspaceId, User user) {
        Workspace workspace = workspaceRepository.findActiveWorkspaceById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException("워크스페이스를 찾을 수 없습니다."));

        if (!canUserAccessWorkspace(workspace, user)) {
            throw new PageAccessDeniedException("워크스페이스에 접근할 권한이 없습니다.");
        }

        return workspace;
    }

    private Page getAccessiblePage(Long pageId, User user) {
        Page page = pageRepository.findActivePageById(pageId)
                .orElseThrow(() -> new PageNotFoundException("페이지를 찾을 수 없습니다."));

        if (!canUserAccessWorkspace(page.getWorkspace(), user)) {
            throw new PageAccessDeniedException("페이지에 접근할 권한이 없습니다.");
        }

        return page;
    }

    private boolean canUserAccessWorkspace(Workspace workspace, User user) {
        return workspace.isOwner(user) ||
                workspace.isMember(user) ||
                workspace.getVisibility() == Workspace.Visibility.PUBLIC;
    }

    // ===== 응답 DTO 내부 클래스들 =====

    /**
     * 페이지 히스토리 응답 DTO
     */
    public static class PageHistoryResponse {
        private final Long id;
        private final Integer version;
        private final String title;
        private final String content;
        private final String summary;
        private final String icon;
        private final String coverImageUrl;
        private final String modifiedByName;
        private final String changeType;
        private final String changeDescription;
        private final Long contentSize;
        private final LocalDateTime createdAt;

        public PageHistoryResponse(Long id, Integer version, String title, String content, String summary,
                                   String icon, String coverImageUrl, String modifiedByName, String changeType,
                                   String changeDescription, Long contentSize, LocalDateTime createdAt) {
            this.id = id;
            this.version = version;
            this.title = title;
            this.content = content;
            this.summary = summary;
            this.icon = icon;
            this.coverImageUrl = coverImageUrl;
            this.modifiedByName = modifiedByName;
            this.changeType = changeType;
            this.changeDescription = changeDescription;
            this.contentSize = contentSize;
            this.createdAt = createdAt;
        }

        public static PageHistoryResponse from(PageHistory history) {
            return new PageHistoryResponse(
                    history.getId(),
                    history.getVersion(),
                    history.getTitle(),
                    history.getContent(),
                    history.getSummary(),
                    history.getIcon(),
                    history.getCoverImageUrl(),
                    history.getModifiedBy().getUsername(),
                    history.getChangeType().name(),
                    history.getChangeDescription(),
                    history.getContentSize(),
                    history.getCreatedAt()
            );
        }

        // Getters
        public Long getId() { return id; }
        public Integer getVersion() { return version; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public String getSummary() { return summary; }
        public String getIcon() { return icon; }
        public String getCoverImageUrl() { return coverImageUrl; }
        public String getModifiedByName() { return modifiedByName; }
        public String getChangeType() { return changeType; }
        public String getChangeDescription() { return changeDescription; }
        public Long getContentSize() { return contentSize; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }

    /**
     * 페이지 통계 응답 DTO
     */
    public static class PageStatisticsResponse {
        private final long totalPages;
        private final long publishedPages;
        private final long templatePages;
        private final long lockedPages;

        public PageStatisticsResponse(long totalPages, long publishedPages, long templatePages, long lockedPages) {
            this.totalPages = totalPages;
            this.publishedPages = publishedPages;
            this.templatePages = templatePages;
            this.lockedPages = lockedPages;
        }

        public long getTotalPages() { return totalPages; }
        public long getPublishedPages() { return publishedPages; }
        public long getTemplatePages() { return templatePages; }
        public long getLockedPages() { return lockedPages; }
    }
}