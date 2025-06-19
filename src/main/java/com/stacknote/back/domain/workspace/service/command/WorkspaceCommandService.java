package com.stacknote.back.domain.workspace.service.command;

import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.domain.user.exception.UserNotFoundException;
import com.stacknote.back.domain.user.repository.UserRepository;
import com.stacknote.back.domain.workspace.dto.request.MemberInviteRequest;
import com.stacknote.back.domain.workspace.dto.request.MemberRoleUpdateRequest;
import com.stacknote.back.domain.workspace.dto.request.WorkspaceCreateRequest;
import com.stacknote.back.domain.workspace.dto.request.WorkspaceUpdateRequest;
import com.stacknote.back.domain.workspace.dto.response.WorkspaceMemberResponse;
import com.stacknote.back.domain.workspace.dto.response.WorkspaceResponse;
import com.stacknote.back.domain.workspace.entity.Workspace;
import com.stacknote.back.domain.workspace.entity.WorkspaceMember;
import com.stacknote.back.domain.workspace.exception.WorkspaceAccessDeniedException;
import com.stacknote.back.domain.workspace.exception.WorkspaceNotFoundException;
import com.stacknote.back.domain.workspace.repository.WorkspaceMemberRepository;
import com.stacknote.back.domain.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 워크스페이스 관련 명령 서비스
 * 워크스페이스 생성, 수정, 삭제, 멤버 관리 등의 명령 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WorkspaceCommandService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;

    /**
     * 워크스페이스 생성
     */
    public WorkspaceResponse createWorkspace(User owner, WorkspaceCreateRequest request) {
        log.info("워크스페이스 생성 시도: {}, 소유자: {}", request.getName(), owner.getId());

        Workspace workspace = Workspace.builder()
                .name(request.getName())
                .description(request.getDescription())
                .icon(request.getIcon())
                .coverImageUrl(request.getCoverImageUrl())
                .owner(owner)
                .visibility(request.getVisibility())
                .build();

        Workspace savedWorkspace = workspaceRepository.save(workspace);

        // 소유자를 멤버로 추가
        WorkspaceMember ownerMember = WorkspaceMember.builder()
                .workspace(savedWorkspace)
                .user(owner)
                .role(WorkspaceMember.Role.OWNER)
                .build();
        workspaceMemberRepository.save(ownerMember);

        log.info("워크스페이스 생성 완료: {}", savedWorkspace.getId());
        return WorkspaceResponse.fromWithPermissions(savedWorkspace, WorkspaceMember.Role.OWNER);
    }

    /**
     * 워크스페이스 정보 수정
     */
    public WorkspaceResponse updateWorkspace(Long workspaceId, User currentUser, WorkspaceUpdateRequest request) {
        log.info("워크스페이스 수정 시도: {}, 사용자: {}", workspaceId, currentUser.getId());

        Workspace workspace = getWorkspaceWithPermissionCheck(workspaceId, currentUser, true);

        workspace.updateInfo(
                request.getName(),
                request.getDescription(),
                request.getIcon(),
                request.getCoverImageUrl()
        );

        if (request.getVisibility() != null) {
            workspace.changeVisibility(request.getVisibility());
        }

        Workspace updatedWorkspace = workspaceRepository.save(workspace);
        WorkspaceMember.Role currentUserRole = workspace.getMemberRole(currentUser);

        log.info("워크스페이스 수정 완료: {}", workspaceId);
        return WorkspaceResponse.fromWithPermissions(updatedWorkspace, currentUserRole);
    }

    /**
     * 워크스페이스 삭제 (소프트 삭제)
     */
    public void deleteWorkspace(Long workspaceId, User currentUser) {
        log.info("워크스페이스 삭제 시도: {}, 사용자: {}", workspaceId, currentUser.getId());

        Workspace workspace = getWorkspaceWithOwnerCheck(workspaceId, currentUser);

        // 워크스페이스 소프트 삭제
        workspace.markAsDeleted();
        workspaceRepository.save(workspace);

        // 모든 멤버 비활성화
        workspaceMemberRepository.deactivateAllByWorkspace(workspace);

        log.info("워크스페이스 삭제 완료: {}", workspaceId);
    }

    /**
     * 멤버 초대
     */
    public WorkspaceMemberResponse inviteMember(Long workspaceId, User inviter, MemberInviteRequest request) {
        log.info("멤버 초대 시도: {}, 초대자: {}, 이메일: {}", workspaceId, inviter.getId(), request.getEmail());

        Workspace workspace = getWorkspaceWithPermissionCheck(workspaceId, inviter, true);

        // 초대할 사용자 조회
        User invitee = userRepository.findActiveUserByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("초대할 사용자를 찾을 수 없습니다."));

        // 이미 멤버인지 확인
        if (workspaceMemberRepository.existsActiveByWorkspaceAndUser(workspace, invitee)) {
            throw new IllegalArgumentException("이미 워크스페이스의 멤버입니다.");
        }

        // 멤버 추가
        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(workspace)
                .user(invitee)
                .role(request.getRole())
                .invitedBy(inviter)
                .build();

        WorkspaceMember savedMember = workspaceMemberRepository.save(member);

        log.info("멤버 초대 완료: {}, 멤버: {}", workspaceId, invitee.getId());
        return WorkspaceMemberResponse.from(savedMember);
    }

    /**
     * 멤버 역할 변경
     */
    public WorkspaceMemberResponse updateMemberRole(Long workspaceId, Long memberId, User currentUser, MemberRoleUpdateRequest request) {
        log.info("멤버 역할 변경 시도: {}, 멤버ID: {}, 사용자: {}", workspaceId, memberId, currentUser.getId());

        Workspace workspace = getWorkspaceWithPermissionCheck(workspaceId, currentUser, true);

        WorkspaceMember member = workspaceMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다."));

        // 멤버가 해당 워크스페이스 소속인지 확인
        if (!member.getWorkspace().getId().equals(workspaceId)) {
            throw new IllegalArgumentException("해당 워크스페이스의 멤버가 아닙니다.");
        }

        // 소유자는 역할 변경 불가
        if (member.getRole() == WorkspaceMember.Role.OWNER) {
            throw new IllegalArgumentException("소유자의 역할은 변경할 수 없습니다.");
        }

        member.changeRole(request.getRole());
        WorkspaceMember updatedMember = workspaceMemberRepository.save(member);

        log.info("멤버 역할 변경 완료: {}, 멤버ID: {}", workspaceId, memberId);
        return WorkspaceMemberResponse.from(updatedMember);
    }

    /**
     * 멤버 제거
     */
    public void removeMember(Long workspaceId, Long memberId, User currentUser) {
        log.info("멤버 제거 시도: {}, 멤버ID: {}, 사용자: {}", workspaceId, memberId, currentUser.getId());

        Workspace workspace = getWorkspaceWithPermissionCheck(workspaceId, currentUser, true);

        WorkspaceMember member = workspaceMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다."));

        // 소유자는 제거 불가
        if (member.getRole() == WorkspaceMember.Role.OWNER) {
            throw new IllegalArgumentException("소유자는 제거할 수 없습니다.");
        }

        member.deactivate();
        workspaceMemberRepository.save(member);

        log.info("멤버 제거 완료: {}, 멤버ID: {}", workspaceId, memberId);
    }

    /**
     * 워크스페이스 나가기
     */
    public void leaveWorkspace(Long workspaceId, User currentUser) {
        log.info("워크스페이스 나가기 시도: {}, 사용자: {}", workspaceId, currentUser.getId());

        Workspace workspace = getActiveWorkspace(workspaceId);

        // 소유자는 나갈 수 없음
        if (workspace.isOwner(currentUser)) {
            throw new IllegalArgumentException("소유자는 워크스페이스를 나갈 수 없습니다. 먼저 다른 멤버에게 소유권을 이전하세요.");
        }

        WorkspaceMember member = workspaceMemberRepository.findActiveByWorkspaceAndUser(workspace, currentUser)
                .orElseThrow(() -> new IllegalArgumentException("워크스페이스의 멤버가 아닙니다."));

        member.deactivate();
        workspaceMemberRepository.save(member);

        log.info("워크스페이스 나가기 완료: {}, 사용자: {}", workspaceId, currentUser.getId());
    }

    /**
     * 초대 코드 생성
     */
    public String generateInviteCode(Long workspaceId, User currentUser) {
        log.info("초대 코드 생성 시도: {}, 사용자: {}", workspaceId, currentUser.getId());

        Workspace workspace = getWorkspaceWithPermissionCheck(workspaceId, currentUser, true);

        String inviteCode = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        workspace.setInviteCode(inviteCode);
        workspaceRepository.save(workspace);

        log.info("초대 코드 생성 완료: {}", workspaceId);
        return inviteCode;
    }

    /**
     * 초대 코드 제거
     */
    public void removeInviteCode(Long workspaceId, User currentUser) {
        log.info("초대 코드 제거 시도: {}, 사용자: {}", workspaceId, currentUser.getId());

        Workspace workspace = getWorkspaceWithPermissionCheck(workspaceId, currentUser, true);

        workspace.removeInviteCode();
        workspaceRepository.save(workspace);

        log.info("초대 코드 제거 완료: {}", workspaceId);
    }

    /**
     * 초대 코드로 워크스페이스 참가
     */
    public WorkspaceResponse joinWorkspaceByInviteCode(String inviteCode, User user) {
        log.info("초대 코드로 워크스페이스 참가 시도: {}, 사용자: {}", inviteCode, user.getId());

        Workspace workspace = workspaceRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 초대 코드입니다."));

        // 이미 멤버인지 확인
        if (workspaceMemberRepository.existsActiveByWorkspaceAndUser(workspace, user)) {
            throw new IllegalArgumentException("이미 워크스페이스의 멤버입니다.");
        }

        // 멤버로 추가 (기본 역할: MEMBER)
        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(workspace)
                .user(user)
                .role(WorkspaceMember.Role.MEMBER)
                .build();
        workspaceMemberRepository.save(member);

        log.info("초대 코드로 워크스페이스 참가 완료: {}, 사용자: {}", workspace.getId(), user.getId());
        return WorkspaceResponse.fromWithPermissions(workspace, WorkspaceMember.Role.MEMBER);
    }

    // ===== 내부 헬퍼 메서드 =====

    private Workspace getActiveWorkspace(Long workspaceId) {
        return workspaceRepository.findActiveWorkspaceById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException("워크스페이스를 찾을 수 없습니다."));
    }

    private Workspace getWorkspaceWithOwnerCheck(Long workspaceId, User user) {
        Workspace workspace = getActiveWorkspace(workspaceId);
        if (!workspace.isOwner(user)) {
            throw new WorkspaceAccessDeniedException("워크스페이스 소유자만 수행할 수 있습니다.");
        }
        return workspace;
    }

    private Workspace getWorkspaceWithPermissionCheck(Long workspaceId, User user, boolean requireManagePermission) {
        Workspace workspace = getActiveWorkspace(workspaceId);

        if (!workspace.isMember(user) && !workspace.isOwner(user)) {
            throw new WorkspaceAccessDeniedException("워크스페이스에 접근할 권한이 없습니다.");
        }

        if (requireManagePermission) {
            WorkspaceMember.Role role = workspace.getMemberRole(user);
            if (role != WorkspaceMember.Role.OWNER && role != WorkspaceMember.Role.ADMIN) {
                throw new WorkspaceAccessDeniedException("워크스페이스 관리 권한이 없습니다.");
            }
        }

        return workspace;
    }
}