package com.stacknote.back.domain.workspace.service.query;

import com.stacknote.back.domain.page.entity.Page;
import com.stacknote.back.domain.page.entity.PageFavorite;
import com.stacknote.back.domain.page.entity.PageVisit;
import com.stacknote.back.domain.page.repository.PageFavoriteRepository;
import com.stacknote.back.domain.page.repository.PageRepository;
import com.stacknote.back.domain.page.repository.PageVisitRepository;
import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.domain.workspace.dto.response.*;
import com.stacknote.back.domain.workspace.entity.Workspace;
import com.stacknote.back.domain.workspace.entity.WorkspaceMember;
import com.stacknote.back.domain.workspace.exception.WorkspaceAccessDeniedException;
import com.stacknote.back.domain.workspace.exception.WorkspaceNotFoundException;
import com.stacknote.back.domain.workspace.repository.WorkspaceMemberRepository;
import com.stacknote.back.domain.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 워크스페이스 사이드바 서비스
 * 노션 스타일의 사이드바를 위한 데이터 조회 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkspaceSidebarService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final PageRepository pageRepository;
    private final PageVisitRepository pageVisitRepository;
    private final PageFavoriteRepository pageFavoriteRepository;

    /**
     * 사이드바 전체 트리 구조 조회
     */
    public WorkspaceSidebarResponse getSidebarTree(User currentUser) {
        log.debug("사이드바 트리 조회 시작: 사용자 {}", currentUser.getId());

        // 사용자가 속한 모든 워크스페이스 조회
        List<Workspace> userWorkspaces = workspaceRepository.findWorkspacesByUser(currentUser);

        // 개인 공간과 팀 공간 분리
        PersonalSpaceResponse personalSpace = null;
        List<TeamSpaceResponse> teamSpaces = new ArrayList<>();

        for (Workspace workspace : userWorkspaces) {
            // 개인 워크스페이스 판별 (소유자이고 이름이 "사용자명의 워크스페이스" 형태)
            if (workspace.isOwner(currentUser) && isPersonalWorkspace(workspace, currentUser)) {
                personalSpace = buildPersonalSpace(workspace);
            } else {
                TeamSpaceResponse teamSpace = buildTeamSpace(workspace, currentUser);
                teamSpaces.add(teamSpace);
            }
        }

        // 최근 방문 페이지 조회
        List<RecentPageResponse> recentPages = getRecentPages(currentUser, 5);

        // 즐겨찾기 페이지 조회
        List<FavoritePageResponse> favoritePages = getFavoritePages(currentUser);

        return WorkspaceSidebarResponse.builder()
                .personalSpace(personalSpace)
                .teamSpaces(teamSpaces)
                .recentPages(recentPages)
                .favoritePages(favoritePages)
                .build();
    }

    /**
     * 워크스페이스별 페이지 트리 조회
     */
    public List<PageTreeResponse> getWorkspacePageTree(Long workspaceId, User currentUser) {
        log.debug("워크스페이스 페이지 트리 조회: 워크스페이스 {}, 사용자 {}", workspaceId, currentUser.getId());

        // 워크스페이스 접근 권한 확인
        Workspace workspace = workspaceRepository.findActiveWorkspaceById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException("워크스페이스를 찾을 수 없습니다."));

        if (!workspace.isMember(currentUser) && workspace.getVisibility() != Workspace.Visibility.PUBLIC) {
            throw new WorkspaceAccessDeniedException("워크스페이스 접근 권한이 없습니다.");
        }

        // 최상위 페이지들 조회
        List<Page> rootPages = pageRepository.findRootPagesByWorkspace(workspace);

        // 페이지 트리 구성
        return rootPages.stream()
                .map(this::buildPageTree)
                .collect(Collectors.toList());
    }

    /**
     * 최근 방문 페이지 조회
     */
    public List<RecentPageResponse> getRecentPages(User currentUser, int limit) {
        log.debug("최근 방문 페이지 조회: 사용자 {}, 개수 {}", currentUser.getId(), limit);

        List<PageVisit> recentVisits = pageVisitRepository.findRecentVisitsByUser(
                currentUser, PageRequest.of(0, limit)
        );

        return recentVisits.stream()
                .map(this::convertToRecentPageResponse)
                .collect(Collectors.toList());
    }

    /**
     * 즐겨찾기 페이지 조회
     */
    public List<FavoritePageResponse> getFavoritePages(User currentUser) {
        log.debug("즐겨찾기 페이지 조회: 사용자 {}", currentUser.getId());

        List<PageFavorite> favorites = pageFavoriteRepository.findByUser(currentUser);

        return favorites.stream()
                .map(this::convertToFavoritePageResponse)
                .collect(Collectors.toList());
    }

    // ===== Private Helper Methods =====

    /**
     * 개인 워크스페이스 여부 확인
     */
    private boolean isPersonalWorkspace(Workspace workspace, User user) {
        // 일반적으로 "사용자명의 워크스페이스" 형태로 생성됨
        String expectedName = user.getUsername() + "의 워크스페이스";
        return workspace.getName().equals(expectedName) ||
                workspace.getName().equals(user.getUsername() + "'s Workspace");
    }

    /**
     * 개인 공간 응답 생성
     */
    private PersonalSpaceResponse buildPersonalSpace(Workspace workspace) {
        List<Page> rootPages = pageRepository.findRootPagesByWorkspace(workspace);
        List<PageTreeResponse> pageTree = rootPages.stream()
                .map(this::buildPageTree)
                .collect(Collectors.toList());

        int totalPageCount = (int) pageRepository.countByWorkspace(workspace);

        return PersonalSpaceResponse.builder()
                .workspaceId(workspace.getId())
                .name(workspace.getName())
                .icon(workspace.getIcon() != null ? workspace.getIcon() : "👤")
                .pages(pageTree)
                .totalPageCount(totalPageCount)
                .build();
    }

    /**
     * 팀 공간 응답 생성
     */
    private TeamSpaceResponse buildTeamSpace(Workspace workspace, User currentUser) {
        // 현재 사용자의 역할 조회
        WorkspaceMember.Role userRole = workspace.getMemberRole(currentUser);

        // 멤버 수 조회 (멤버 테이블의 활성 멤버 수 + 소유자 1명)
        long activeMemberCount = workspaceMemberRepository.countActiveMembers(workspace.getId());
        int memberCount = (int) (activeMemberCount + 1); // 소유자 포함

        // 페이지 수 조회
        int totalPageCount = (int) pageRepository.countByWorkspace(workspace);

        return TeamSpaceResponse.builder()
                .workspaceId(workspace.getId())
                .name(workspace.getName())
                .icon(workspace.getIcon() != null ? workspace.getIcon() : "🏢")
                .currentUserRole(userRole)
                .memberCount(memberCount)
                .pages(new ArrayList<>()) // 초기에는 빈 리스트, 확장 시 로드
                .totalPageCount(totalPageCount)
                .isExpanded(false)
                .build();
    }

    /**
     * 페이지 트리 구성
     */
    private PageTreeResponse buildPageTree(Page page) {
        PageTreeResponse response = PageTreeResponse.builder()
                .id(page.getId())
                .title(page.getTitle())
                .icon(page.getIcon())
                .parentId(page.getParent() != null ? page.getParent().getId() : null)
                .depth(page.getDepth())
                .sortOrder(page.getSortOrder())
                .hasChildren(!page.getChildren().isEmpty())
                .isPublished(page.getIsPublished())
                .isLocked(page.getIsLocked())
                .children(new ArrayList<>())
                .build();

        // 자식 페이지들 재귀적으로 구성
        if (!page.getChildren().isEmpty()) {
            List<PageTreeResponse> children = page.getChildren().stream()
                    .filter(child -> child.getDeletedAt() == null)
                    .map(this::buildPageTree)
                    .collect(Collectors.toList());
            response.setChildren(children);
        }

        return response;
    }

    /**
     * PageVisit을 RecentPageResponse로 변환
     */
    private RecentPageResponse convertToRecentPageResponse(PageVisit visit) {
        Page page = visit.getPage();
        return RecentPageResponse.builder()
                .pageId(page.getId())
                .title(page.getTitle())
                .icon(page.getIcon())
                .workspaceId(page.getWorkspace().getId())
                .workspaceName(page.getWorkspace().getName())
                .lastVisitedAt(visit.getVisitedAt())
                .build();
    }

    /**
     * PageFavorite을 FavoritePageResponse로 변환
     */
    private FavoritePageResponse convertToFavoritePageResponse(PageFavorite favorite) {
        Page page = favorite.getPage();
        return FavoritePageResponse.builder()
                .pageId(page.getId())
                .title(page.getTitle())
                .icon(page.getIcon())
                .workspaceId(page.getWorkspace().getId())
                .workspaceName(page.getWorkspace().getName())
                .favoritedAt(favorite.getCreatedAt())
                .build();
    }
}