package com.stacknote.back.domain.search.service;

import com.stacknote.back.domain.page.entity.Page;
import com.stacknote.back.domain.page.repository.PageRepository;
import com.stacknote.back.domain.search.dto.request.SearchType;
import com.stacknote.back.domain.search.dto.response.GlobalSearchResponse;
import com.stacknote.back.domain.search.dto.response.SearchResultGroup;
import com.stacknote.back.domain.search.dto.response.SearchResultItem;
import com.stacknote.back.domain.search.dto.response.SearchSuggestion;
import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.domain.workspace.entity.Workspace;
import com.stacknote.back.domain.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 전역 검색 서비스
 * 워크스페이스와 페이지를 통합하여 검색하는 기능 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GlobalSearchService {

    private final WorkspaceRepository workspaceRepository;
    private final PageRepository pageRepository;

    /**
     * 전역 검색
     */
    public GlobalSearchResponse search(String query, Long workspaceId, SearchType type, User currentUser) {
        log.debug("전역 검색 시작: 검색어={}, 워크스페이스={}, 타입={}, 사용자={}",
                query, workspaceId, type, currentUser.getId());

        if (query == null || query.trim().length() < 2) {
            return GlobalSearchResponse.builder()
                    .results(new ArrayList<>())
                    .totalCount(0)
                    .build();
        }

        String keyword = query.trim();
        Map<Long, SearchResultGroup> groupMap = new HashMap<>();
        int totalCount = 0;

        // 특정 워크스페이스 검색
        if (workspaceId != null) {
            Workspace workspace = workspaceRepository.findActiveWorkspaceById(workspaceId).orElse(null);
            if (workspace != null && workspace.isMember(currentUser)) {
                SearchResultGroup group = searchInWorkspace(workspace, keyword, type, currentUser);
                if (!group.getItems().isEmpty()) {
                    groupMap.put(workspace.getId(), group);
                    totalCount += group.getItems().size();
                }
            }
        } else {
            // 모든 접근 가능한 워크스페이스 검색
            List<Workspace> userWorkspaces = workspaceRepository.findWorkspacesByUser(currentUser);

            for (Workspace workspace : userWorkspaces) {
                SearchResultGroup group = searchInWorkspace(workspace, keyword, type, currentUser);
                if (!group.getItems().isEmpty()) {
                    groupMap.put(workspace.getId(), group);
                    totalCount += group.getItems().size();
                }
            }
        }

        // 결과를 리스트로 변환
        List<SearchResultGroup> results = new ArrayList<>(groupMap.values());

        return GlobalSearchResponse.builder()
                .results(results)
                .totalCount(totalCount)
                .build();
    }

    /**
     * 검색 제안
     */
    public List<SearchSuggestion> getSuggestions(String query, int limit, User currentUser) {
        log.debug("검색 제안 요청: 검색어={}, 개수={}, 사용자={}", query, limit, currentUser.getId());

        if (query == null || query.trim().length() < 1) {
            return new ArrayList<>();
        }

        String keyword = query.trim().toLowerCase();
        List<SearchSuggestion> suggestions = new ArrayList<>();

        // 사용자가 접근 가능한 워크스페이스들
        List<Workspace> userWorkspaces = workspaceRepository.findWorkspacesByUser(currentUser);

        // 워크스페이스 이름 제안
        userWorkspaces.stream()
                .filter(w -> w.getName().toLowerCase().contains(keyword))
                .limit(limit / 3)
                .forEach(w -> suggestions.add(SearchSuggestion.builder()
                        .text(w.getName())
                        .type("WORKSPACE")
                        .icon(w.getIcon() != null ? w.getIcon() : "🏢")
                        .build()));

        // 페이지 제목 제안
        for (Workspace workspace : userWorkspaces) {
            List<Page> pages = pageRepository.searchByTitleInWorkspace(workspace, keyword);
            pages.stream()
                    .limit(limit - suggestions.size())
                    .forEach(p -> suggestions.add(SearchSuggestion.builder()
                            .text(p.getTitle())
                            .type("PAGE")
                            .icon(p.getIcon() != null ? p.getIcon() : "📄")
                            .build()));

            if (suggestions.size() >= limit) break;
        }

        // 중복 제거 및 제한
        return suggestions.stream()
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ===== Private Helper Methods =====

    /**
     * 특정 워크스페이스 내 검색
     */
    private SearchResultGroup searchInWorkspace(Workspace workspace, String keyword, SearchType type, User currentUser) {
        SearchResultGroup group = SearchResultGroup.builder()
                .workspaceId(workspace.getId())
                .workspaceName(workspace.getName())
                .items(new ArrayList<>())
                .build();

        // 워크스페이스 검색
        if (type == SearchType.ALL || type == SearchType.WORKSPACE) {
            if (workspace.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                    (workspace.getDescription() != null && workspace.getDescription().toLowerCase().contains(keyword.toLowerCase()))) {

                SearchResultItem item = SearchResultItem.builder()
                        .id(workspace.getId())
                        .type("WORKSPACE")
                        .title(workspace.getName())
                        .icon(workspace.getIcon() != null ? workspace.getIcon() : "🏢")
                        .highlight(highlightText(workspace.getName(), keyword))
                        .path(workspace.getName())
                        .build();

                group.getItems().add(item);
            }
        }

        // 페이지 검색
        if (type == SearchType.ALL || type == SearchType.PAGE || type == SearchType.TITLE || type == SearchType.CONTENT) {
            List<Page> pages;

            if (type == SearchType.TITLE) {
                pages = pageRepository.searchByTitleInWorkspace(workspace, keyword);
            } else {
                pages = pageRepository.searchByContentInWorkspace(workspace, keyword);
            }

            for (Page page : pages) {
                SearchResultItem item = SearchResultItem.builder()
                        .id(page.getId())
                        .type("PAGE")
                        .title(page.getTitle())
                        .icon(page.getIcon() != null ? page.getIcon() : "📄")
                        .highlight(extractHighlight(page, keyword))
                        .path(buildPagePath(page))
                        .build();

                group.getItems().add(item);
            }
        }

        return group;
    }

    /**
     * 검색어 하이라이트 처리
     */
    private String highlightText(String text, String keyword) {
        if (text == null || keyword == null) return text;

        // 간단한 하이라이트 처리 (실제로는 더 복잡한 로직 필요)
        int index = text.toLowerCase().indexOf(keyword.toLowerCase());
        if (index >= 0) {
            int start = Math.max(0, index - 20);
            int end = Math.min(text.length(), index + keyword.length() + 20);
            return "..." + text.substring(start, end) + "...";
        }

        return text;
    }

    /**
     * 페이지에서 하이라이트 추출
     */
    private String extractHighlight(Page page, String keyword) {
        // 제목에서 찾기
        if (page.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
            return highlightText(page.getTitle(), keyword);
        }

        // 내용에서 찾기
        if (page.getContent() != null && page.getContent().toLowerCase().contains(keyword.toLowerCase())) {
            return highlightText(page.getContent(), keyword);
        }

        // 요약에서 찾기
        if (page.getSummary() != null) {
            return page.getSummary();
        }

        return page.getTitle();
    }

    /**
     * 페이지 경로 구성
     */
    private String buildPagePath(Page page) {
        List<String> pathElements = new ArrayList<>();
        Page current = page;

        // 페이지 계층 구조를 역순으로 추가
        while (current != null) {
            pathElements.add(0, current.getTitle());
            current = current.getParent();
        }

        // 워크스페이스 이름 추가
        pathElements.add(0, page.getWorkspace().getName());

        return String.join(" > ", pathElements);
    }
}