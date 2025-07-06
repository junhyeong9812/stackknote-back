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

    // ===== 추가된 메서드들 =====

    /**
     * 사용자의 개인 워크스페이스 조회
     * 일반적으로 "사용자명의 워크스페이스" 형태의 이름을 가진 워크스페이스
     */
    @Query("""
        SELECT w FROM Workspace w 
        WHERE w.owner = :user 
        AND w.deletedAt IS NULL 
        AND w.isActive = true 
        AND (w.name = :personalName1 OR w.name = :personalName2)
        """)
    Optional<Workspace> findPersonalWorkspace(
            @Param("user") User user,
            @Param("personalName1") String personalName1,
            @Param("personalName2") String personalName2
    );

    /**
     * 워크스페이스와 멤버 정보를 함께 조회 (N+1 문제 해결)
     */
    @Query("""
        SELECT DISTINCT w FROM Workspace w 
        LEFT JOIN FETCH w.members m 
        LEFT JOIN FETCH m.user 
        WHERE w.id = :workspaceId 
        AND w.deletedAt IS NULL 
        AND w.isActive = true
        """)
    Optional<Workspace> findWorkspaceWithMembers(@Param("workspaceId") Long workspaceId);

    /**
     * 사용자가 속한 워크스페이스 목록을 역할과 함께 조회
     */
    @Query("""
        SELECT w, CASE 
            WHEN w.owner = :user THEN 'OWNER'
            ELSE m.role 
        END as userRole
        FROM Workspace w 
        LEFT JOIN w.members m ON m.user = :user AND m.isActive = true
        WHERE w.deletedAt IS NULL 
        AND w.isActive = true 
        AND (w.owner = :user OR m.user = :user)
        ORDER BY w.updatedAt DESC
        """)
    List<Object[]> findWorkspacesWithUserRole(@Param("user") User user);

    /**
     * 워크스페이스 이름과 설명으로 검색 (전역 검색용)
     */
    @Query("""
        SELECT w FROM Workspace w 
        WHERE w.deletedAt IS NULL 
        AND w.isActive = true 
        AND (w.visibility = 'PUBLIC' OR w.owner = :user OR EXISTS (
            SELECT 1 FROM WorkspaceMember m 
            WHERE m.workspace = w AND m.user = :user AND m.isActive = true
        ))
        AND (LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
             OR LOWER(w.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY w.updatedAt DESC
        """)
    List<Workspace> searchAccessibleWorkspaces(@Param("user") User user, @Param("keyword") String keyword);
}