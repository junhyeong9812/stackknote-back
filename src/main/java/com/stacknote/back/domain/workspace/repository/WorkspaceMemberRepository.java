package com.stacknote.back.domain.workspace.repository;

import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.domain.workspace.entity.Workspace;
import com.stacknote.back.domain.workspace.entity.WorkspaceMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 워크스페이스 멤버 Repository
 */
@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {

    /**
     * 워크스페이스의 활성 멤버 목록 조회
     */
    @Query("SELECT m FROM WorkspaceMember m WHERE m.workspace = :workspace AND m.isActive = true ORDER BY m.createdAt")
    List<WorkspaceMember> findActiveByWorkspace(@Param("workspace") Workspace workspace);

    /**
     * 워크스페이스의 활성 멤버 목록 조회 (페이징)
     */
    @Query("SELECT m FROM WorkspaceMember m WHERE m.workspace = :workspace AND m.isActive = true ORDER BY m.createdAt")
    Page<WorkspaceMember> findActiveByWorkspace(@Param("workspace") Workspace workspace, Pageable pageable);

    /**
     * 특정 워크스페이스에서 사용자의 멤버십 조회
     */
    @Query("SELECT m FROM WorkspaceMember m WHERE m.workspace = :workspace AND m.user = :user AND m.isActive = true")
    Optional<WorkspaceMember> findActiveByWorkspaceAndUser(@Param("workspace") Workspace workspace, @Param("user") User user);

    /**
     * 사용자가 속한 모든 워크스페이스 멤버십 조회
     */
    @Query("SELECT m FROM WorkspaceMember m WHERE m.user = :user AND m.isActive = true ORDER BY m.createdAt DESC")
    List<WorkspaceMember> findActiveByUser(@Param("user") User user);

    /**
     * 워크스페이스에서 특정 역할의 멤버 수 조회
     */
    @Query("SELECT COUNT(m) FROM WorkspaceMember m WHERE m.workspace = :workspace AND m.role = :role AND m.isActive = true")
    long countByWorkspaceAndRole(@Param("workspace") Workspace workspace, @Param("role") WorkspaceMember.Role role);

    /**
     * 사용자가 특정 워크스페이스의 멤버인지 확인
     */
    @Query("SELECT COUNT(m) > 0 FROM WorkspaceMember m WHERE m.workspace = :workspace AND m.user = :user AND m.isActive = true")
    boolean existsActiveByWorkspaceAndUser(@Param("workspace") Workspace workspace, @Param("user") User user);

    /**
     * 워크스페이스의 모든 멤버 비활성화 (워크스페이스 삭제 시)
     */
    @Modifying
    @Query("UPDATE WorkspaceMember m SET m.isActive = false WHERE m.workspace = :workspace")
    int deactivateAllByWorkspace(@Param("workspace") Workspace workspace);

    /**
     * 사용자를 워크스페이스에서 제거 (비활성화)
     */
    @Modifying
    @Query("UPDATE WorkspaceMember m SET m.isActive = false WHERE m.workspace = :workspace AND m.user = :user")
    int deactivateByWorkspaceAndUser(@Param("workspace") Workspace workspace, @Param("user") User user);

    /**
     * 워크스페이스에서 특정 역할을 가진 활성 멤버 목록 조회
     */
    @Query("SELECT m FROM WorkspaceMember m WHERE m.workspace = :workspace AND m.role = :role AND m.isActive = true")
    List<WorkspaceMember> findActiveByWorkspaceAndRole(@Param("workspace") Workspace workspace, @Param("role") WorkspaceMember.Role role);

    /**
     * 이메일로 워크스페이스 멤버 검색
     */
    @Query("""
        SELECT m FROM WorkspaceMember m 
        WHERE m.workspace = :workspace 
        AND m.isActive = true 
        AND LOWER(m.user.email) LIKE LOWER(CONCAT('%', :email, '%'))
        ORDER BY m.createdAt
        """)
    List<WorkspaceMember> searchByWorkspaceAndEmail(@Param("workspace") Workspace workspace, @Param("email") String email);

    /**
     * 워크스페이스의 관리자(소유자 + 관리자) 목록 조회
     */
    @Query("""
        SELECT m FROM WorkspaceMember m 
        WHERE m.workspace = :workspace 
        AND m.isActive = true 
        AND m.role IN ('OWNER', 'ADMIN')
        ORDER BY m.role DESC, m.createdAt
        """)
    List<WorkspaceMember> findAdminsByWorkspace(@Param("workspace") Workspace workspace);

    // ===== 추가된 메서드들 =====

    /**
     * 워크스페이스 ID로 활성 멤버 수 조회 (소유자 제외)
     */
    @Query("""
        SELECT COUNT(m) FROM WorkspaceMember m 
        WHERE m.workspace.id = :workspaceId 
        AND m.isActive = true
        """)
    long countActiveMembers(@Param("workspaceId") Long workspaceId);

    /**
     * 워크스페이스의 총 멤버 수 조회 (소유자 포함)
     * 소유자는 WorkspaceMember 테이블에 없으므로 +1 해야 함
     */
    @Query("""
        SELECT COUNT(m) + 1 FROM WorkspaceMember m 
        WHERE m.workspace.id = :workspaceId 
        AND m.isActive = true
        """)
    long countTotalMembers(@Param("workspaceId") Long workspaceId);

    /**
     * 사용자가 속한 워크스페이스 ID 목록 조회
     */
    @Query("""
        SELECT m.workspace.id FROM WorkspaceMember m 
        WHERE m.user = :user 
        AND m.isActive = true
        """)
    List<Long> findWorkspaceIdsByUser(@Param("user") User user);

    /**
     * 워크스페이스 멤버와 사용자 정보를 함께 조회 (N+1 문제 해결)
     */
    @Query("""
        SELECT m FROM WorkspaceMember m 
        JOIN FETCH m.user u 
        WHERE m.workspace = :workspace 
        AND m.isActive = true 
        ORDER BY m.role DESC, m.createdAt
        """)
    List<WorkspaceMember> findActiveByWorkspaceWithUser(@Param("workspace") Workspace workspace);

    /**
     * 특정 사용자가 특정 역할을 가진 워크스페이스 수 조회
     */
    @Query("""
        SELECT COUNT(m) FROM WorkspaceMember m 
        WHERE m.user = :user 
        AND m.role = :role 
        AND m.isActive = true
        """)
    long countByUserAndRole(@Param("user") User user, @Param("role") WorkspaceMember.Role role);
}