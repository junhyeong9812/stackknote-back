package com.stacknote.back.domain.page.repository;

import com.stacknote.back.domain.page.entity.Page;
import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.domain.workspace.entity.Workspace;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 페이지 Repository
 */
@Repository
public interface PageRepository extends JpaRepository<Page, Long> {

    /**
     * 워크스페이스의 활성 페이지 목록 조회
     */
    @Query("SELECT p FROM Page p WHERE p.workspace = :workspace AND p.deletedAt IS NULL ORDER BY p.sortOrder, p.createdAt")
    List<Page> findByWorkspace(@Param("workspace") Workspace workspace);

    /**
     * 워크스페이스의 최상위 페이지들 조회
     */
    @Query("SELECT p FROM Page p WHERE p.workspace = :workspace AND p.parent IS NULL AND p.deletedAt IS NULL ORDER BY p.sortOrder, p.createdAt")
    List<Page> findRootPagesByWorkspace(@Param("workspace") Workspace workspace);

    /**
     * 특정 부모 페이지의 자식 페이지들 조회
     */
    @Query("SELECT p FROM Page p WHERE p.parent = :parent AND p.deletedAt IS NULL ORDER BY p.sortOrder, p.createdAt")
    List<Page> findByParent(@Param("parent") Page parent);

    /**
     * 페이지 ID로 활성 페이지 조회
     */
    @Query("SELECT p FROM Page p WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Page> findActivePageById(@Param("id") Long id);

    /**
     * 워크스페이스 내에서 제목으로 페이지 검색
     */
    @Query("""
        SELECT p FROM Page p 
        WHERE p.workspace = :workspace 
        AND p.deletedAt IS NULL 
        AND LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY p.updatedAt DESC
        """)
    List<Page> searchByTitleInWorkspace(@Param("workspace") Workspace workspace, @Param("keyword") String keyword);

    /**
     * 워크스페이스 내에서 콘텐츠로 페이지 검색
     */
    @Query("""
        SELECT p FROM Page p 
        WHERE p.workspace = :workspace 
        AND p.deletedAt IS NULL 
        AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) 
             OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY p.updatedAt DESC
        """)
    List<Page> searchByContentInWorkspace(@Param("workspace") Workspace workspace, @Param("keyword") String keyword);

    /**
     * 사용자가 생성한 페이지 목록 조회
     */
    @Query("SELECT p FROM Page p WHERE p.createdBy = :user AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Page> findByCreatedBy(@Param("user") User user);

    /**
     * 최근 수정된 페이지 목록 조회 (워크스페이스별)
     */
    @Query("""
        SELECT p FROM Page p 
        WHERE p.workspace = :workspace 
        AND p.deletedAt IS NULL 
        AND p.updatedAt >= :since
        ORDER BY p.updatedAt DESC
        """)
    List<Page> findRecentlyModifiedInWorkspace(@Param("workspace") Workspace workspace, @Param("since") LocalDateTime since, Pageable pageable);

    /**
     * 공개된 페이지 목록 조회 (워크스페이스별)
     */
    @Query("SELECT p FROM Page p WHERE p.workspace = :workspace AND p.isPublished = true AND p.deletedAt IS NULL ORDER BY p.updatedAt DESC")
    List<Page> findPublishedPagesByWorkspace(@Param("workspace") Workspace workspace);

    /**
     * 템플릿 페이지 목록 조회 (워크스페이스별)
     */
    @Query("SELECT p FROM Page p WHERE p.workspace = :workspace AND p.isTemplate = true AND p.deletedAt IS NULL ORDER BY p.title")
    List<Page> findTemplatesByWorkspace(@Param("workspace") Workspace workspace);

    /**
     * 페이지 타입별 조회
     */
    @Query("SELECT p FROM Page p WHERE p.workspace = :workspace AND p.pageType = :pageType AND p.deletedAt IS NULL ORDER BY p.sortOrder, p.createdAt")
    List<Page> findByWorkspaceAndPageType(@Param("workspace") Workspace workspace, @Param("pageType") Page.PageType pageType);

    /**
     * 특정 사용자가 마지막으로 수정한 페이지들 조회
     */
    @Query("""
        SELECT p FROM Page p 
        WHERE p.lastModifiedBy = :user 
        AND p.deletedAt IS NULL 
        ORDER BY p.updatedAt DESC
        """)
    List<Page> findRecentlyModifiedByUser(@Param("user") User user, Pageable pageable);

    /**
     * 워크스페이스의 페이지 개수 조회
     */
    @Query("SELECT COUNT(p) FROM Page p WHERE p.workspace = :workspace AND p.deletedAt IS NULL")
    long countByWorkspace(@Param("workspace") Workspace workspace);

    /**
     * 특정 부모 페이지의 자식 페이지 개수 조회
     */
    @Query("SELECT COUNT(p) FROM Page p WHERE p.parent = :parent AND p.deletedAt IS NULL")
    long countByParent(@Param("parent") Page parent);

    /**
     * 조회수 증가
     */
    @Modifying
    @Query("UPDATE Page p SET p.viewCount = p.viewCount + 1 WHERE p.id = :pageId")
    int incrementViewCount(@Param("pageId") Long pageId);

    /**
     * 부모 페이지의 모든 자식 페이지 조회 (재귀적)
     */
    @Query(value = """
        WITH RECURSIVE page_tree AS (
            SELECT id, title, parent_id, workspace_id, 0 as level
            FROM pages 
            WHERE parent_id = :parentId AND deleted_at IS NULL
            UNION ALL
            SELECT p.id, p.title, p.parent_id, p.workspace_id, pt.level + 1
            FROM pages p
            INNER JOIN page_tree pt ON p.parent_id = pt.id
            WHERE p.deleted_at IS NULL
        )
        SELECT id FROM page_tree ORDER BY level, id
        """, nativeQuery = true)
    List<Long> findAllDescendantIds(@Param("parentId") Long parentId);

    /**
     * 워크스페이스의 최대 정렬 순서 조회
     */
    @Query("SELECT COALESCE(MAX(p.sortOrder), 0) FROM Page p WHERE p.workspace = :workspace AND p.parent IS NULL AND p.deletedAt IS NULL")
    int findMaxSortOrderInWorkspace(@Param("workspace") Workspace workspace);

    /**
     * 특정 부모의 최대 정렬 순서 조회
     */
    @Query("SELECT COALESCE(MAX(p.sortOrder), 0) FROM Page p WHERE p.parent = :parent AND p.deletedAt IS NULL")
    int findMaxSortOrderByParent(@Param("parent") Page parent);

    /**
     * 잠긴 페이지 목록 조회
     */
    @Query("SELECT p FROM Page p WHERE p.workspace = :workspace AND p.isLocked = true AND p.deletedAt IS NULL")
    List<Page> findLockedPagesByWorkspace(@Param("workspace") Workspace workspace);

    /**
     * 인기 페이지 목록 조회 (조회수 기준)
     */
    @Query("SELECT p FROM Page p WHERE p.workspace = :workspace AND p.deletedAt IS NULL ORDER BY p.viewCount DESC, p.updatedAt DESC")
    List<Page> findPopularPagesByWorkspace(@Param("workspace") Workspace workspace, Pageable pageable);
}