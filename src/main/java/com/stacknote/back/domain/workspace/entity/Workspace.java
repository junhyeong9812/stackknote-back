package com.stacknote.back.domain.workspace.entity;

import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 워크스페이스 엔티티
 * 사용자들이 협업할 수 있는 작업 공간
 */
@Entity
@Table(name = "workspaces", indexes = {
        @Index(name = "idx_workspace_name", columnList = "name"),
        @Index(name = "idx_workspace_owner", columnList = "owner_id"),
        @Index(name = "idx_workspace_is_active", columnList = "is_active")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Workspace extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "icon", length = 10)
    private String icon; // 이모지 또는 아이콘 식별자

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    @Builder.Default
    private Visibility visibility = Visibility.PRIVATE;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "invite_code", unique = true, length = 50)
    private String inviteCode; // 초대 링크용 코드

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkspaceMember> members = new ArrayList<>();

    // ===== 비즈니스 로직 메서드 =====

    /**
     * 워크스페이스 정보 업데이트
     */
    public void updateInfo(String name, String description, String icon, String coverImageUrl) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
        if (description != null) {
            this.description = description.trim().isEmpty() ? null : description.trim();
        }
        if (icon != null) {
            this.icon = icon.trim().isEmpty() ? null : icon.trim();
        }
        if (coverImageUrl != null) {
            this.coverImageUrl = coverImageUrl.trim().isEmpty() ? null : coverImageUrl.trim();
        }
    }

    /**
     * 가시성 변경
     */
    public void changeVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    /**
     * 워크스페이스 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 워크스페이스 활성화
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * 초대 코드 설정
     */
    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    /**
     * 초대 코드 제거
     */
    public void removeInviteCode() {
        this.inviteCode = null;
    }

    /**
     * 소유자 여부 확인
     */
    public boolean isOwner(User user) {
        return this.owner.getId().equals(user.getId());
    }

    /**
     * 멤버 여부 확인
     */
    public boolean isMember(User user) {
        return members.stream()
                .anyMatch(member -> member.getUser().getId().equals(user.getId()) && member.isActive());
    }

    /**
     * 멤버 권한 조회
     */
    public WorkspaceMember.Role getMemberRole(User user) {
        if (isOwner(user)) {
            return WorkspaceMember.Role.OWNER;
        }

        return members.stream()
                .filter(member -> member.getUser().getId().equals(user.getId()) && member.isActive())
                .map(WorkspaceMember::getRole)
                .findFirst()
                .orElse(null);
    }

    /**
     * 멤버 추가
     */
    public void addMember(User user, WorkspaceMember.Role role) {
        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(this)
                .user(user)
                .role(role)
                .build();
        this.members.add(member);
    }

    /**
     * 멤버 제거
     */
    public void removeMember(User user) {
        this.members.removeIf(member -> member.getUser().getId().equals(user.getId()));
    }

    /**
     * 워크스페이스 가시성
     */
    public enum Visibility {
        PRIVATE,    // 비공개 (초대된 멤버만 접근)
        PUBLIC      // 공개 (누구나 접근 가능)
    }
}