package com.stacknote.back.domain.workspace.entity;

import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 워크스페이스 멤버 엔티티
 * 워크스페이스와 사용자 간의 연관관계 및 권한 관리
 */
@Entity
@Table(name = "workspace_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"workspace_id", "user_id"}),
        indexes = {
                @Index(name = "idx_workspace_member_workspace", columnList = "workspace_id"),
                @Index(name = "idx_workspace_member_user", columnList = "user_id"),
                @Index(name = "idx_workspace_member_role", columnList = "role")
        })
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class WorkspaceMember extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by")
    private User invitedBy; // 초대한 사용자

    // ===== Getter 메서드 명시적 정의 =====

    public Boolean getIsActive() {
        return this.isActive;
    }

    public Boolean isActive() {
        return this.isActive;
    }

    // ===== 비즈니스 로직 메서드 =====

    /**
     * 역할 변경
     */
    public void changeRole(Role role) {
        this.role = role;
    }

    /**
     * 멤버 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 멤버 활성화
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * 읽기 권한 확인
     */
    public boolean canRead() {
        return isActive && (role == Role.OWNER || role == Role.ADMIN || role == Role.MEMBER || role == Role.VIEWER);
    }

    /**
     * 쓰기 권한 확인
     */
    public boolean canWrite() {
        return isActive && (role == Role.OWNER || role == Role.ADMIN || role == Role.MEMBER);
    }

    /**
     * 관리 권한 확인
     */
    public boolean canManage() {
        return isActive && (role == Role.OWNER || role == Role.ADMIN);
    }

    /**
     * 소유자 권한 확인
     */
    public boolean isOwner() {
        return isActive && role == Role.OWNER;
    }

    /**
     * 워크스페이스 멤버 역할
     */
    public enum Role {
        OWNER,      // 소유자 (모든 권한)
        ADMIN,      // 관리자 (멤버 관리, 설정 변경 가능)
        MEMBER,     // 멤버 (읽기/쓰기 가능)
        VIEWER      // 뷰어 (읽기만 가능)
    }
}