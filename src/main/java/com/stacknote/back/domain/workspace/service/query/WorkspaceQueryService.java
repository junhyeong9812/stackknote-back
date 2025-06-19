package com.stacknote.back.domain.workspace.service.query;

import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.domain.workspace.dto.response.WorkspaceMemberResponse;
import com.stacknote.back.domain.workspace.dto.response.WorkspaceResponse;
import com.stacknote.back.domain.workspace.dto.response.WorkspaceSummaryResponse;
import com.stacknote.back.domain.workspace.entity.Workspace;
import com.stacknote.back.domain.workspace.entity.WorkspaceMember;
import com.stacknote.back.domain.workspace.exception.WorkspaceAccessDeniedException;
import com.stacknote.back.domain.workspace.exception.WorkspaceNotFoundException;
import com.stacknote.back.domain.workspace.repository.WorkspaceMemberRepository;
import com.stacknote.back.domain.workspace.repository.WorkspaceRepository;
import com.stacknote.back.global.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 워크스페이스 관련 쿼리 서비스
 * 워크스페이스 조회, 검색 등의 읽기 전용 작업 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkspaceQueryService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    /**
     * 사용자의 워크스페이스 목록 조회
     */
    public List<WorkspaceSummaryResponse> getUserWorkspaces(User user) {
        log.debug("사용자 워크스페이스 목록 조회: {}", user.getId());

        List<Workspace> workspaces = workspaceRepository.findWorkspacesByUser(user);

        return workspaces.stream()
                .map(workspace -> {
                    WorkspaceMember.Role userRole = workspace.getMemberRole(user);
                    long memberCount = workspaceRepository.countMembersByWorkspaceId(workspace.getId());
                    // TODO: 페이지 수 조회는 Page 도메인 구현 후 추가
                    long pageCount = 0L;

                    return WorkspaceSummaryResponse.of(
                            workspace.getId(),
                            workspace.getName(),
                            workspace.getDescription(),
                            workspace.getIcon(),
                            workspace.getCoverImageUrl(),
                            workspace.getOwner().getUsername(),
                            workspace.getVisibility().name(),
                            workspace.getIsActive(),
                            userRole != null ? userRole.name() : null,
                            memberCount,
                            pageCount
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * 워크스페이스 상세 조회
     */
    public WorkspaceResponse getWorkspace(Long workspaceId, User currentUser) {
        log.debug("워크스페이스 상세 조회: {}, 사용자: {}", workspaceId, currentUser.getId());

        Workspace workspace = workspaceRepository.findActiveWorkspaceById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException("워크스페이스를 찾을 수 없습니다."));

        // 접근 권한 확인
        if (!canUserAccessWorkspace(workspace, currentUser)) {
            throw new WorkspaceAccessDeniedException("워크스페이스에 접근할 권한이 없습니다.");
        }

        WorkspaceMember.Role currentUserRole = workspace.getMemberRole(currentUser);
        return WorkspaceResponse.fromWithPermissions(workspace, currentUserRole);
    }

    /**
     * 워크스페이스 멤버 목록 조회
     */
    public List<WorkspaceMemberResponse> getWorkspaceMembers(Long workspaceId, User currentUser) {
        log.debug("워크스페이스 멤버 목록 조회: {}, 사용자: {}", workspaceId, currentUser.getId());

        Workspace workspace = getAccessibleWorkspace(workspaceId, currentUser);

        List<WorkspaceMember> members = workspaceMemberRepository.findActiveByWorkspace(workspace);

        return members.stream()
                .map(WorkspaceMemberResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 워크스페이스 멤버 목록 조회 (페이징)
     */
    public PageResponse<WorkspaceMemberResponse> getWorkspaceMembers(Long workspaceId, User currentUser, Pageable pageable) {
        log.debug("워크스페이스 멤버 목록 조회 (페이징): {}, 사용자: {}", workspaceId, currentUser.getId());

        Workspace workspace = getAccessibleWorkspace(workspaceId, currentUser);

        Page<WorkspaceMember> memberPage = workspaceMemberRepository.findActiveByWorkspace(workspace, pageable);
        Page<WorkspaceMemberResponse> responsePage = memberPage.map(WorkspaceMemberResponse::from);

        return PageResponse.of(responsePage);
    }

    /**
     * 워크스페이스 검색
     */
    public List<WorkspaceSummaryResponse> searchWorkspaces(User user, String keyword) {
        log.debug("워크스페이스 검색: 사용자: {}, 키워드: {}", user.getId(), keyword);

        List<Workspace> workspaces = workspaceRepository.searchWorkspacesByName(user, keyword);

        return workspaces.stream()
                .map(workspace -> {
                    WorkspaceMember.Role userRole = workspace.getMemberRole(user);
                    long memberCount = workspaceRepository.countMembersByWorkspaceId(workspace.getId());
                    long pageCount = 0L; // TODO: Page 도메인 구현 후 추가

                    return WorkspaceSummaryResponse.of(
                            workspace.getId(),
                            workspace.getName(),
                            workspace.getDescription(),
                            workspace.getIcon(),
                            workspace.getCoverImageUrl(),
                            workspace.getOwner().getUsername(),
                            workspace.getVisibility().name(),
                            workspace.getIsActive(),
                            userRole != null ? userRole.name() : null,
                            memberCount,
                            pageCount
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * 공개 워크스페이스 목록 조회
     */
    public PageResponse<WorkspaceResponse> getPublicWorkspaces(Pageable pageable) {
        log.debug("공개 워크스페이스 목록 조회");

        Page<Workspace> workspacePage = workspaceRepository.findPublicWorkspaces(pageable);
        Page<WorkspaceResponse> responsePage = workspacePage.map(WorkspaceResponse::from);

        return PageResponse.of(responsePage);
    }

    /**
     * 사용자의 워크스페이스 통계 조회
     */
    public WorkspaceStatisticsResponse getUserWorkspaceStatistics(User user) {
        log.debug("사용자 워크스페이스 통계 조회: {}", user.getId());

        long totalWorkspaces = workspaceRepository.countWorkspacesByUser(user);
        long ownedWorkspaces = workspaceRepository.findByOwner(user).size();
        long memberWorkspaces = totalWorkspaces - ownedWorkspaces;

        return new WorkspaceStatisticsResponse(totalWorkspaces, ownedWorkspaces, memberWorkspaces);
    }

    /**
     * 초대 코드로 워크스페이스 정보 조회 (참가 전 미리보기)
     */
    public WorkspaceResponse getWorkspaceByInviteCode(String inviteCode) {
        log.debug("초대 코드로 워크스페이스 조회: {}", inviteCode);

        Workspace workspace = workspaceRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 초대 코드입니다."));

        return WorkspaceResponse.from(workspace);
    }

    // ===== 내부 헬퍼 메서드 =====

    private Workspace getAccessibleWorkspace(Long workspaceId, User user) {
        Workspace workspace = workspaceRepository.findActiveWorkspaceById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException("워크스페이스를 찾을 수 없습니다."));

        if (!canUserAccessWorkspace(workspace, user)) {
            throw new WorkspaceAccessDeniedException("워크스페이스에 접근할 권한이 없습니다.");
        }

        return workspace;
    }

    private boolean canUserAccessWorkspace(Workspace workspace, User user) {
        return workspace.isOwner(user) ||
                workspace.isMember(user) ||
                workspace.getVisibility() == Workspace.Visibility.PUBLIC;
    }

    /**
     * 워크스페이스 통계 응답 DTO
     */
    public static class WorkspaceStatisticsResponse {
        private final long totalWorkspaces;
        private final long ownedWorkspaces;
        private final long memberWorkspaces;

        public WorkspaceStatisticsResponse(long totalWorkspaces, long ownedWorkspaces, long memberWorkspaces) {
            this.totalWorkspaces = totalWorkspaces;
            this.ownedWorkspaces = ownedWorkspaces;
            this.memberWorkspaces = memberWorkspaces;
        }

        public long getTotalWorkspaces() { return totalWorkspaces; }
        public long getOwnedWorkspaces() { return ownedWorkspaces; }
        public long getMemberWorkspaces() { return memberWorkspaces; }
    }
}