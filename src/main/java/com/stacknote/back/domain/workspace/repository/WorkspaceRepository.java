package com.stacknote.back.domain.workspace.repository;

import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.domain.workspace.entity.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 워크스페이스 Repository
 */
@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    /**
     * 사용자가 소유한 워크스페이스 목록 조회
     */
    @Query("SELECT w FROM Workspace w WHERE w.owner = :owner AND w.deletedAt IS NULL ORDER BY w.createdAt DESC")
    List<Workspace> findByOwner(@Param("owner") User owner);

    /**
     * 사용자가 멤버로 속한 워크스페이스 목록 조회 (소유한 것 포함)
     */
    @Query("""
        SELECT DISTINCT w FROM Workspace w 
        LEFT JOIN w.members m 
        WHERE w.deletedAt IS NULL 
        AND w.isActive = true 
        AND (w.owner = :user OR (m.user = :user AND m.isActive = true))
        ORDER BY w.updatedAt DESC
        """)
    List<Workspace> findWorkspacesByUser(@Param("user") User user);

    /**
     * 사용자가 멤버로 속한 워크스페이스 목록 조회 (페이징)
     */
    @Query("""
        SELECT DISTINCT w FROM Workspace w 
        LEFT JOIN w.members m 
        WHERE w.deletedAt IS NULL 
        AND w.isActive = true 
        AND (w.owner = :user OR (m.user = :user AND m.isActive = true))
        """)
    Page<Workspace> findWorkspacesByUser(@Param("user") User user, Pageable pageable);

    /**
     * 워크스페이스 ID로 활성 상태인 워크스페이스 조회
     */
    @Query("SELECT w FROM Workspace w WHERE w.id = :id AND w.deletedAt IS NULL AND w.isActive = true")
    Optional<Workspace> findActiveWorkspaceById(@Param("id") Long id);

    /**
     * 초대 코드로 워크스페이스 조회
     */
    @Query("SELECT w FROM Workspace w WHERE w.inviteCode = :inviteCode AND w.deletedAt IS NULL AND w.isActive = true")
    Optional<Workspace> findByInviteCode(@Param("inviteCode") String inviteCode);

    /**
     * 워크스페이스 이름으로 검색 (사용자가 접근 가능한 워크스페이스 중)
     */
    @Query("""
        SELECT DISTINCT w FROM Workspace w 
        LEFT JOIN w.members m 
        WHERE w.deletedAt IS NULL 
        AND w.isActive = true 
        AND (w.owner = :user OR (m.user = :user AND m.isActive = true))
        AND LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY w.updatedAt DESC
        """)
    List<Workspace> searchWorkspacesByName(@Param("user") User user, @Param("keyword") String keyword);

    /**
     * 공개 워크스페이스 목록 조회
     */
    @Query("""
        SELECT w FROM Workspace w 
        WHERE w.visibility = 'PUBLIC' 
        AND w.deletedAt IS NULL 
        AND w.isActive = true 
        ORDER BY w.createdAt DESC
        """)
    Page<Workspace> findPublicWorkspaces(Pageable pageable);

    /**
     * 사용자가 특정 워크스페이스에 접근 가능한지 확인
     */
    @Query("""
        SELECT COUNT(w) > 0 FROM Workspace w 
        LEFT JOIN w.members m 
        WHERE w.id = :workspaceId 
        AND w.deletedAt IS NULL 
        AND w.isActive = true 
        AND (w.owner = :user OR (m.user = :user AND m.isActive = true) OR w.visibility = 'PUBLIC')
        """)
    boolean canUserAccessWorkspace(@Param("user") User user, @Param("workspaceId") Long workspaceId);

    /**
     * 워크스페이스 멤버 수 조회
     */
    @Query("""
        SELECT COUNT(m) FROM WorkspaceMember m 
        WHERE m.workspace.id = :workspaceId 
        AND m.isActive = true
        """)
    long countMembersByWorkspaceId(@Param("workspaceId") Long workspaceId);

    /**
     * 사용자의 워크스페이스 개수 조회
     */
    @Query("""
        SELECT COUNT(DISTINCT w) FROM Workspace w 
        LEFT JOIN w.members m 
        WHERE w.deletedAt IS NULL 
        AND w.isActive = true 
        AND (w.owner = :user OR (m.user = :user AND m.isActive = true))
        """)
    long countWorkspacesByUser(@Param("user") User user);
}