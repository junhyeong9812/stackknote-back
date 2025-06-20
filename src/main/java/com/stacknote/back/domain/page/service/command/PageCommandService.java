package com.stacknote.back.domain.page.service.command;

import com.stacknote.back.domain.page.dto.request.PageCreateRequest;
import com.stacknote.back.domain.page.dto.request.PageDuplicateRequest;
import com.stacknote.back.domain.page.dto.request.PageMoveRequest;
import com.stacknote.back.domain.page.dto.request.PageUpdateRequest;
import com.stacknote.back.domain.page.dto.response.PageResponse;
import com.stacknote.back.domain.page.entity.Page;
import com.stacknote.back.domain.page.entity.PageHistory;
import com.stacknote.back.domain.page.exception.PageAccessDeniedException;
import com.stacknote.back.domain.page.exception.PageLockedException;
import com.stacknote.back.domain.page.exception.PageNotFoundException;
import com.stacknote.back.domain.page.repository.PageHistoryRepository;
import com.stacknote.back.domain.page.repository.PageRepository;
import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.domain.workspace.entity.Workspace;
import com.stacknote.back.domain.workspace.entity.WorkspaceMember;
import com.stacknote.back.domain.workspace.exception.WorkspaceNotFoundException;
import com.stacknote.back.domain.workspace.repository.WorkspaceMemberRepository;
import com.stacknote.back.domain.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 페이지 관련 명령 서비스
 * 페이지 생성, 수정, 삭제, 이동 등의 명령 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PageCommandService {

    private final PageRepository pageRepository;
    private final PageHistoryRepository pageHistoryRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    /**
     * 페이지 생성
     */
    public PageResponse createPage(Long workspaceId, User currentUser, PageCreateRequest request) {
        log.info("페이지 생성 시도: {}, 워크스페이스: {}, 사용자: {}", request.getTitle(), workspaceId, currentUser.getId());

        Workspace workspace = getWorkspaceWithWritePermission(workspaceId, currentUser);
        Page parent = null;

        if (request.getParentId() != null) {
            parent = getPageWithPermissionCheck(request.getParentId(), currentUser, false);
            if (!parent.getWorkspace().getId().equals(workspaceId)) {
                throw new IllegalArgumentException("부모 페이지가 다른 워크스페이스에 속해있습니다.");
            }
        }

        // 정렬 순서 자동 설정
        int sortOrder = request.getSortOrder();
        if (sortOrder == 0) {
            sortOrder = parent != null ?
                    pageRepository.findMaxSortOrderByParent(parent) + 1 :
                    pageRepository.findMaxSortOrderInWorkspace(workspace) + 1;
        }

        Page page = Page.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .icon(request.getIcon())
                .coverImageUrl(request.getCoverImageUrl())
                .workspace(workspace)
                .parent(parent)
                .createdBy(currentUser)
                .lastModifiedBy(currentUser)
                .isPublished(request.getIsPublished())
                .isTemplate(request.getIsTemplate())
                .sortOrder(sortOrder)
                .pageType(request.getPageType())
                .build();

        Page savedPage = pageRepository.save(page);

        // 히스토리 생성
        createPageHistory(savedPage, currentUser, PageHistory.ChangeType.CREATED, "페이지 생성");

        log.info("페이지 생성 완료: {}", savedPage.getId());
        return PageResponse.from(savedPage);
    }

    /**
     * 페이지 수정
     */
    public PageResponse updatePage(Long pageId, User currentUser, PageUpdateRequest request) {
        log.info("페이지 수정 시도: {}, 사용자: {}", pageId, currentUser.getId());

        Page page = getPageWithPermissionCheck(pageId, currentUser, true);

        if (page.getIsLocked()) {
            throw new PageLockedException("페이지가 잠겨있어 편집할 수 없습니다.");
        }

        // 변경 사항 추적
        String changeDescription = buildChangeDescription(page, request);
        PageHistory.ChangeType changeType = determineChangeType(page, request);

        // 페이지 정보 업데이트
        page.updateInfo(request.getTitle(), request.getContent(), request.getIcon(), request.getCoverImageUrl());
        page.updateLastModifiedBy(currentUser);

        if (request.getPageType() != null) {
            page.changePageType(request.getPageType());
        }
        if (request.getIsPublished() != null) {
            if (request.getIsPublished()) {
                page.publish();
            } else {
                page.unpublish();
            }
        }
        if (request.getIsTemplate() != null) {
            if (request.getIsTemplate()) {
                page.markAsTemplate();
            } else {
                page.unmarkAsTemplate();
            }
        }
        if (request.getIsLocked() != null) {
            if (request.getIsLocked()) {
                page.lock();
            } else {
                page.unlock();
            }
        }
        if (request.getSortOrder() != null) {
            page.updateSortOrder(request.getSortOrder());
        }

        Page updatedPage = pageRepository.save(page);

        // 히스토리 생성 (변경 사항이 있는 경우만)
        if (changeType != null) {
            createPageHistory(updatedPage, currentUser, changeType, changeDescription);
        }

        log.info("페이지 수정 완료: {}", pageId);
        return PageResponse.from(updatedPage);
    }

    /**
     * 페이지 삭제 (소프트 삭제)
     */
    public void deletePage(Long pageId, User currentUser) {
        log.info("페이지 삭제 시도: {}, 사용자: {}", pageId, currentUser.getId());

        Page page = getPageWithPermissionCheck(pageId, currentUser, true);

        if (page.getIsLocked()) {
            throw new PageLockedException("페이지가 잠겨있어 삭제할 수 없습니다.");
        }

        // 자식 페이지들도 함께 삭제
        List<Long> descendantIds = pageRepository.findAllDescendantIds(pageId);
        for (Long descendantId : descendantIds) {
            Page descendant = pageRepository.findById(descendantId).orElse(null);
            if (descendant != null) {
                descendant.markAsDeleted();
                pageRepository.save(descendant);
            }
        }

        // 현재 페이지 삭제
        page.markAsDeleted();
        pageRepository.save(page);

        log.info("페이지 삭제 완료: {}", pageId);
    }

    /**
     * 페이지 이동
     */
    public PageResponse movePage(Long pageId, User currentUser, PageMoveRequest request) {
        log.info("페이지 이동 시도: {}, 사용자: {}", pageId, currentUser.getId());

        Page page = getPageWithPermissionCheck(pageId, currentUser, true);

        if (page.getIsLocked()) {
            throw new PageLockedException("페이지가 잠겨있어 이동할 수 없습니다.");
        }

        Page newParent = null;
        if (request.getNewParentId() != null) {
            newParent = getPageWithPermissionCheck(request.getNewParentId(), currentUser, false);

            // 순환 참조 방지
            if (isDescendantOf(newParent, page)) {
                throw new IllegalArgumentException("자식 페이지를 부모로 이동할 수 없습니다.");
            }

            // 같은 워크스페이스 내에서만 이동 가능
            if (!newParent.getWorkspace().getId().equals(page.getWorkspace().getId())) {
                throw new IllegalArgumentException("다른 워크스페이스로는 이동할 수 없습니다.");
            }
        }

        // 정렬 순서 설정
        int sortOrder = request.getNewSortOrder() != null ?
                request.getNewSortOrder() :
                (newParent != null ?
                        pageRepository.findMaxSortOrderByParent(newParent) + 1 :
                        pageRepository.findMaxSortOrderInWorkspace(page.getWorkspace()) + 1);

        page.setParent(newParent);
        page.updateSortOrder(sortOrder);
        page.updateLastModifiedBy(currentUser);

        Page movedPage = pageRepository.save(page);

        // 히스토리 생성
        createPageHistory(movedPage, currentUser, PageHistory.ChangeType.STRUCTURE_CHANGED, "페이지 이동");

        log.info("페이지 이동 완료: {}", pageId);
        return PageResponse.from(movedPage);
    }

    /**
     * 페이지 복제
     */
    public PageResponse duplicatePage(Long pageId, User currentUser, PageDuplicateRequest request) {
        log.info("페이지 복제 시도: {}, 사용자: {}", pageId, currentUser.getId());

        Page originalPage = getPageWithPermissionCheck(pageId, currentUser, false);
        Workspace workspace = originalPage.getWorkspace();

        // 복제 권한 확인
        checkWorkspaceWritePermission(workspace, currentUser);

        Page newParent = null;
        if (request.getNewParentId() != null) {
            newParent = getPageWithPermissionCheck(request.getNewParentId(), currentUser, false);
            if (!newParent.getWorkspace().getId().equals(workspace.getId())) {
                throw new IllegalArgumentException("부모 페이지가 다른 워크스페이스에 속해있습니다.");
            }
        } else {
            newParent = originalPage.getParent();
        }

        // 새 제목 설정
        String newTitle = request.getNewTitle() != null ?
                request.getNewTitle() :
                "Copy of " + originalPage.getTitle();

        // 정렬 순서 설정
        int sortOrder = newParent != null ?
                pageRepository.findMaxSortOrderByParent(newParent) + 1 :
                pageRepository.findMaxSortOrderInWorkspace(workspace) + 1;

        Page duplicatedPage = Page.builder()
                .title(newTitle)
                .content(originalPage.getContent())
                .icon(originalPage.getIcon())
                .coverImageUrl(originalPage.getCoverImageUrl())
                .workspace(workspace)
                .parent(newParent)
                .createdBy(currentUser)
                .lastModifiedBy(currentUser)
                .isPublished(false) // 복제된 페이지는 기본적으로 비공개
                .isTemplate(originalPage.getIsTemplate())
                .sortOrder(sortOrder)
                .pageType(originalPage.getPageType())
                .viewCount(request.getResetViewCount() ? 0L : originalPage.getViewCount())
                .build();

        Page savedPage = pageRepository.save(duplicatedPage);

        // 자식 페이지들도 함께 복제 (옵션)
        if (request.getIncludeChildren()) {
            duplicateChildren(originalPage, savedPage, currentUser);
        }

        // 히스토리 생성
        createPageHistory(savedPage, currentUser, PageHistory.ChangeType.CREATED, "페이지 복제 (원본: " + originalPage.getTitle() + ")");

        log.info("페이지 복제 완료: {} -> {}", pageId, savedPage.getId());
        return PageResponse.from(savedPage);
    }

    /**
     * 페이지 상태 변경 (공개/비공개)
     */
    public PageResponse togglePageVisibility(Long pageId, User currentUser) {
        log.info("페이지 공개 상태 변경 시도: {}, 사용자: {}", pageId, currentUser.getId());

        Page page = getPageWithPermissionCheck(pageId, currentUser, true);

        if (page.getIsPublished()) {
            page.unpublish();
        } else {
            page.publish();
        }

        page.updateLastModifiedBy(currentUser);
        Page updatedPage = pageRepository.save(page);

        // 히스토리 생성
        String description = page.getIsPublished() ? "페이지 공개" : "페이지 비공개";
        createPageHistory(updatedPage, currentUser, PageHistory.ChangeType.STATUS_CHANGED, description);

        log.info("페이지 공개 상태 변경 완료: {}", pageId);
        return PageResponse.from(updatedPage);
    }

    /**
     * 페이지 잠금/잠금 해제
     */
    public PageResponse togglePageLock(Long pageId, User currentUser) {
        log.info("페이지 잠금 상태 변경 시도: {}, 사용자: {}", pageId, currentUser.getId());

        Page page = getPageWithPermissionCheck(pageId, currentUser, true);

        if (page.getIsLocked()) {
            page.unlock();
        } else {
            page.lock();
        }

        page.updateLastModifiedBy(currentUser);
        Page updatedPage = pageRepository.save(page);

        // 히스토리 생성
        String description = page.getIsLocked() ? "페이지 잠금" : "페이지 잠금 해제";
        createPageHistory(updatedPage, currentUser, PageHistory.ChangeType.STATUS_CHANGED, description);

        log.info("페이지 잠금 상태 변경 완료: {}", pageId);
        return PageResponse.from(updatedPage);
    }

    /**
     * 페이지를 이전 버전으로 복원
     */
    public PageResponse restorePageVersion(Long pageId, Integer version, User currentUser) {
        log.info("페이지 버전 복원 시도: {}, 버전: {}, 사용자: {}", pageId, version, currentUser.getId());

        Page page = getPageWithPermissionCheck(pageId, currentUser, true);

        if (page.getIsLocked()) {
            throw new PageLockedException("페이지가 잠겨있어 복원할 수 없습니다.");
        }

        PageHistory history = pageHistoryRepository.findByPageAndVersion(page, version)
                .orElseThrow(() -> new IllegalArgumentException("해당 버전을 찾을 수 없습니다."));

        // 현재 상태를 히스토리로 저장
        createPageHistory(page, currentUser, PageHistory.ChangeType.RESTORED, "버전 " + version + "으로 복원 전 백업");

        // 페이지를 이전 버전으로 복원
        page.updateInfo(history.getTitle(), history.getContent(), history.getIcon(), history.getCoverImageUrl());
        page.updateLastModifiedBy(currentUser);

        Page restoredPage = pageRepository.save(page);

        // 복원 히스토리 생성
        createPageHistory(restoredPage, currentUser, PageHistory.ChangeType.RESTORED, "버전 " + version + "으로 복원");

        log.info("페이지 버전 복원 완료: {}", pageId);
        return PageResponse.from(restoredPage);
    }

    // ===== 내부 헬퍼 메서드 =====

    private Workspace getWorkspaceWithWritePermission(Long workspaceId, User user) {
        Workspace workspace = workspaceRepository.findActiveWorkspaceById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException("워크스페이스를 찾을 수 없습니다."));

        checkWorkspaceWritePermission(workspace, user);
        return workspace;
    }

    private void checkWorkspaceWritePermission(Workspace workspace, User user) {
        if (workspace.isOwner(user)) {
            return; // 소유자는 모든 권한
        }

        WorkspaceMember member = workspaceMemberRepository.findActiveByWorkspaceAndUser(workspace, user)
                .orElseThrow(() -> new PageAccessDeniedException("워크스페이스에 접근할 권한이 없습니다."));

        if (!member.canWrite()) {
            throw new PageAccessDeniedException("페이지를 편집할 권한이 없습니다.");
        }
    }

    private Page getPageWithPermissionCheck(Long pageId, User user, boolean requireWritePermission) {
        Page page = pageRepository.findActivePageById(pageId)
                .orElseThrow(() -> new PageNotFoundException("페이지를 찾을 수 없습니다."));

        Workspace workspace = page.getWorkspace();

        // 읽기 권한 확인
        if (!canUserAccessPage(workspace, user)) {
            throw new PageAccessDeniedException("페이지에 접근할 권한이 없습니다.");
        }

        // 쓰기 권한 확인 (필요한 경우)
        if (requireWritePermission) {
            checkWorkspaceWritePermission(workspace, user);
        }

        return page;
    }

    private boolean canUserAccessPage(Workspace workspace, User user) {
        return workspace.isOwner(user) ||
                workspace.isMember(user) ||
                workspace.getVisibility() == Workspace.Visibility.PUBLIC;
    }

    private void createPageHistory(Page page, User modifiedBy, PageHistory.ChangeType changeType, String description) {
        int nextVersion = pageHistoryRepository.findMaxVersionByPage(page) + 1;

        PageHistory history = PageHistory.builder()
                .page(page)
                .version(nextVersion)
                .title(page.getTitle())
                .content(page.getContent())
                .summary(page.getSummary())
                .icon(page.getIcon())
                .coverImageUrl(page.getCoverImageUrl())
                .modifiedBy(modifiedBy)
                .changeType(changeType)
                .changeDescription(description)
                .contentSize(page.getContent() != null ? (long) page.getContent().getBytes().length : 0L)
                .build();

        pageHistoryRepository.save(history);
    }

    private String buildChangeDescription(Page page, PageUpdateRequest request) {
        StringBuilder description = new StringBuilder();

        if (request.getTitle() != null && !request.getTitle().equals(page.getTitle())) {
            description.append("제목 변경, ");
        }
        if (request.getContent() != null && !request.getContent().equals(page.getContent())) {
            description.append("내용 변경, ");
        }
        if (request.getIcon() != null && !request.getIcon().equals(page.getIcon())) {
            description.append("아이콘 변경, ");
        }
        if (request.getCoverImageUrl() != null && !request.getCoverImageUrl().equals(page.getCoverImageUrl())) {
            description.append("커버 이미지 변경, ");
        }

        if (description.length() > 0) {
            description.setLength(description.length() - 2); // 마지막 ", " 제거
            return description.toString();
        }

        return "메타데이터 변경";
    }

    private PageHistory.ChangeType determineChangeType(Page page, PageUpdateRequest request) {
        if (request.getTitle() != null && !request.getTitle().equals(page.getTitle())) {
            return PageHistory.ChangeType.TITLE_UPDATED;
        }
        if (request.getContent() != null && !request.getContent().equals(page.getContent())) {
            return PageHistory.ChangeType.CONTENT_UPDATED;
        }
        if ((request.getIcon() != null && !request.getIcon().equals(page.getIcon())) ||
                (request.getCoverImageUrl() != null && !request.getCoverImageUrl().equals(page.getCoverImageUrl()))) {
            return PageHistory.ChangeType.METADATA_UPDATED;
        }
        if (request.getIsPublished() != null || request.getIsLocked() != null) {
            return PageHistory.ChangeType.STATUS_CHANGED;
        }

        return null; // 변경 사항 없음
    }

    private boolean isDescendantOf(Page potentialDescendant, Page ancestor) {
        Page current = potentialDescendant.getParent();
        while (current != null) {
            if (current.getId().equals(ancestor.getId())) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    private void duplicateChildren(Page originalParent, Page newParent, User currentUser) {
        List<Page> children = pageRepository.findByParent(originalParent);

        for (Page child : children) {
            Page duplicatedChild = Page.builder()
                    .title(child.getTitle())
                    .content(child.getContent())
                    .icon(child.getIcon())
                    .coverImageUrl(child.getCoverImageUrl())
                    .workspace(newParent.getWorkspace())
                    .parent(newParent)
                    .createdBy(currentUser)
                    .lastModifiedBy(currentUser)
                    .isPublished(false)
                    .isTemplate(child.getIsTemplate())
                    .sortOrder(child.getSortOrder())
                    .pageType(child.getPageType())
                    .viewCount(0L)
                    .build();

            Page savedChild = pageRepository.save(duplicatedChild);
            createPageHistory(savedChild, currentUser, PageHistory.ChangeType.CREATED, "자식 페이지 복제");

            // 재귀적으로 자식의 자식들도 복제
            duplicateChildren(child, savedChild, currentUser);
        }
    }
}