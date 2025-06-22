package com.stacknote.back.domain.tag.repository;

import com.stacknote.back.domain.tag.entity.Tag;
import com.stacknote.back.domain.workspace.entity.Workspace;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 태그 Repository
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * 태그 ID로 활성 태그 조회
     */
    @Query("SELECT t FROM Tag t WHERE t.id = :id AND t.deletedAt IS NULL")
    Optional<Tag> findActiveTagById(@Param("id") Long id);

    /**
     * 워크스페이스의 모든 활성 태그 조회 (사용 횟수 내림차순)
     */
    @Query("SELECT t FROM Tag t WHERE t.workspace = :workspace AND t.deletedAt IS NULL ORDER BY t.usageCount DESC, t.name ASC")
    List<Tag> findTagsByWorkspaceOrderByUsage(@Param("workspace") Workspace workspace);

    /**
     * 워크스페이스의 태그를 이름순으로 조회
     */
    @Query("SELECT t FROM Tag t WHERE t.workspace = :workspace AND t.deletedAt IS NULL ORDER BY t.name ASC")
    List<Tag> findTagsByWorkspaceOrderByName(@Param("workspace") Workspace workspace);

    /**
     * 워크스페이스 내에서 태그 이름으로 조회
     */
    @Query("SELECT t FROM Tag t WHERE t.workspace = :workspace AND t.name = :name AND t.deletedAt IS NULL")
    Optional<Tag> findByWorkspaceAndName(@Param("workspace") Workspace workspace, @Param("name") String name);

    /**
     * 워크스페이스 내에서 태그 이름 존재 여부 확인
     */
    @Query("SELECT COUNT(t) > 0 FROM Tag t WHERE t.workspace = :workspace AND t.name = :name AND t.deletedAt IS NULL")
    boolean existsByWorkspaceAndName(@Param("workspace") Workspace workspace, @Param("name") String name);

    /**
     * 워크스페이스의 인기 태그 조회 (사용 횟수 기준)
     */
    @Query("SELECT t FROM Tag t WHERE t.workspace = :workspace AND t.deletedAt IS NULL AND t.usageCount > 0 ORDER BY t.usageCount DESC")
    List<Tag> findPopularTagsByWorkspace(@Param("workspace") Workspace workspace, Pageable pageable);

    /**
     * 워크스페이스의 사용되지 않는 태그 조회
     */
    @Query("SELECT t FROM Tag t WHERE t.workspace = :workspace AND t.deletedAt IS NULL AND t.usageCount = 0 AND t.isSystemTag = false ORDER BY t.createdAt DESC")
    List<Tag> findUnusedTagsByWorkspace(@Param("workspace") Workspace workspace);

    /**
     * 워크스페이스의 시스템 태그 조회
     */
    @Query("SELECT t FROM Tag t WHERE t.workspace = :workspace AND t.deletedAt IS NULL AND t.isSystemTag = true ORDER BY t.name ASC")
    List<Tag> findSystemTagsByWorkspace(@Param("workspace") Workspace workspace);

    /**
     * 태그 이름으로 검색 (부분 일치)
     */
    @Query("SELECT t FROM Tag t WHERE t.workspace = :workspace AND t.deletedAt IS NULL AND LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY t.usageCount DESC")
    List<Tag> searchTagsByName(@Param("workspace") Workspace workspace, @Param("keyword") String keyword);

    /**
     * 색상별 태그 조회
     */
    @Query("SELECT t FROM Tag t WHERE t.workspace = :workspace AND t.deletedAt IS NULL AND t.color = :color ORDER BY t.name ASC")
    List<Tag> findTagsByColor(@Param("workspace") Workspace workspace, @Param("color") String color);

    /**
     * 워크스페이스의 태그 수 조회
     */
    @Query("SELECT COUNT(t) FROM Tag t WHERE t.workspace = :workspace AND t.deletedAt IS NULL")
    long countTagsByWorkspace(@Param("workspace") Workspace workspace);

    /**
     * 사용 중인 태그 수 조회
     */
    @Query("SELECT COUNT(t) FROM Tag t WHERE t.workspace = :workspace AND t.deletedAt IS NULL AND t.usageCount > 0")
    long countUsedTagsByWorkspace(@Param("workspace") Workspace workspace);

    /**
     * 특정 페이지의 태그들 조회
     */
    @Query("""
        SELECT t FROM Tag t 
        JOIN PageTag pt ON t.id = pt.tag.id 
        WHERE pt.page.id = :pageId AND t.deletedAt IS NULL 
        ORDER BY pt.position ASC, t.name ASC
        """)
    List<Tag> findTagsByPageId(@Param("pageId") Long pageId);

    /**
     * 여러 태그 이름으로 태그들 조회
     */
    @Query("SELECT t FROM Tag t WHERE t.workspace = :workspace AND t.name IN :names AND t.deletedAt IS NULL")
    List<Tag> findTagsByWorkspaceAndNames(@Param("workspace") Workspace workspace, @Param("names") List<String> names);

    /**
     * 특정 사용 횟수 이상의 태그 조회
     */
    @Query("SELECT t FROM Tag t WHERE t.workspace = :workspace AND t.deletedAt IS NULL AND t.usageCount >= :minUsage ORDER BY t.usageCount DESC")
    List<Tag> findTagsWithMinUsage(@Param("workspace") Workspace workspace, @Param("minUsage") int minUsage);

    /**
     * 최근 생성된 태그 조회
     */
    @Query("SELECT t FROM Tag t WHERE t.workspace = :workspace AND t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<Tag> findRecentTagsByWorkspace(@Param("workspace") Workspace workspace, Pageable pageable);

    /**
     * 태그 사용 횟수 업데이트
     */
    @Modifying
    @Query("UPDATE Tag t SET t.usageCount = t.usageCount + :increment WHERE t.id = :tagId")
    void updateUsageCount(@Param("tagId") Long tagId, @Param("increment") int increment);

    /**
     * 워크스페이스 삭제 시 모든 태그 소프트 삭제
     */
    @Modifying
    @Query("UPDATE Tag t SET t.deletedAt = CURRENT_TIMESTAMP WHERE t.workspace = :workspace AND t.deletedAt IS NULL")
    int softDeleteTagsByWorkspace(@Param("workspace") Workspace workspace);

    /**
     * 사용되지 않는 태그들 정리 (배치용)
     */
    @Query("SELECT t FROM Tag t WHERE t.deletedAt IS NULL AND t.usageCount = 0 AND t.isSystemTag = false AND t.createdAt < :cutoffDate")
    List<Tag> findUnusedTagsForCleanup(@Param("cutoffDate") java.time.LocalDateTime cutoffDate, Pageable pageable);

    /**
     * 워크스페이스의 태그 통계 조회
     */
    @Query("""
        SELECT new com.stacknote.back.domain.tag.dto.response.TagStatisticsResponse(
            COUNT(t),
            COUNT(CASE WHEN t.usageCount > 0 THEN 1 END),
            COUNT(CASE WHEN t.isSystemTag = true THEN 1 END),
            COALESCE(AVG(t.usageCount), 0)
        )
        FROM Tag t 
        WHERE t.workspace = :workspace AND t.deletedAt IS NULL
        """)
    Object[] getTagStatistics(@Param("workspace") Workspace workspace);
}