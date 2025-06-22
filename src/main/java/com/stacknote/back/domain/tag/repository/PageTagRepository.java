package com.stacknote.back.domain.tag.repository;

import com.stacknote.back.domain.page.entity.Page;
import com.stacknote.back.domain.tag.entity.PageTag;
import com.stacknote.back.domain.tag.entity.Tag;
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
 * 페이지-태그 연관관계 Repository
 */
@Repository
public interface PageTagRepository extends JpaRepository<PageTag, Long> {

    /**
     * 페이지-태그 연관관계 조회
     */
    Optional<PageTag> findByPageAndTag(Page page, Tag tag);

    /**
     * 페이지-태그 연관관계 존재 여부 확인
     */
    boolean existsByPageAndTag(Page page, Tag tag);

    /**
     * 페이지의 모든 태그 조회 (순서대로)
     */
    @Query("SELECT pt FROM PageTag pt WHERE pt.page = :page ORDER BY pt.position ASC, pt.createdAt ASC")
    List<PageTag> findByPageOrderByPosition(@Param("page") Page page);

    /**
     * 태그가 사용된 모든 페이지 조회
     */
    @Query("SELECT pt FROM PageTag pt WHERE pt.tag = :tag ORDER BY pt.createdAt DESC")
    List<PageTag> findByTagOrderByCreatedAt(@Param("tag") Tag tag);

    /**
     * 특정 태그가 사용된 페이지 수 조회
     */
    @Query("SELECT COUNT(pt) FROM PageTag pt WHERE pt.tag = :tag")
    long countByTag(@Param("tag") Tag tag);

    /**
     * 페이지의 태그 수 조회
     */
    @Query("SELECT COUNT(pt) FROM PageTag pt WHERE pt.page = :page")
    long countByPage(@Param("page") Page page);

    /**
     * 사용자가 추가한 페이지-태그 연관관계 조회
     */
    @Query("SELECT pt FROM PageTag pt WHERE pt.createdBy = :user ORDER BY pt.createdAt DESC")
    List<PageTag> findByCreatedByOrderByCreatedAt(@Param("user") User user, Pageable pageable);

    /**
     * 워크스페이스의 태그 사용 통계 조회
     */
    @Query("""
        SELECT pt.tag.id, pt.tag.name, COUNT(pt) as usageCount
        FROM PageTag pt 
        WHERE pt.page.workspace = :workspace 
        GROUP BY pt.tag.id, pt.tag.name 
        ORDER BY usageCount DESC
        """)
    List<Object[]> getTagUsageStatistics(@Param("workspace") Workspace workspace);

    /**
     * 특정 기간 내 추가된 페이지-태그 연관관계 조회
     */
    @Query("""
        SELECT pt FROM PageTag pt 
        WHERE pt.createdAt BETWEEN :startDate AND :endDate 
        ORDER BY pt.createdAt DESC
        """)
    List<PageTag> findByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 여러 태그를 모두 가진 페이지 조회
     */
    @Query("""
        SELECT pt.page FROM PageTag pt 
        WHERE pt.tag IN :tags 
        GROUP BY pt.page 
        HAVING COUNT(DISTINCT pt.tag) = :tagCount
        """)
    List<Page> findPagesWithAllTags(@Param("tags") List<Tag> tags, @Param("tagCount") long tagCount);

    /**
     * 특정 태그들 중 하나라도 가진 페이지 조회
     */
    @Query("""
        SELECT DISTINCT pt.page FROM PageTag pt 
        WHERE pt.tag IN :tags 
        ORDER BY pt.page.updatedAt DESC
        """)
    List<Page> findPagesWithAnyTags(@Param("tags") List<Tag> tags);

    /**
     * 가장 많이 사용되는 태그 조합 조회
     */
    @Query(value = """
        SELECT STRING_AGG(t.name, ', ' ORDER BY t.name) as tag_combination, COUNT(*) as usage_count
        FROM page_tags pt1 
        JOIN page_tags pt2 ON pt1.page_id = pt2.page_id 
        JOIN tags t ON pt2.tag_id = t.id
        WHERE pt1.page_id IN (
            SELECT page_id FROM page_tags GROUP BY page_id HAVING COUNT(*) > 1
        )
        GROUP BY pt1.page_id
        HAVING COUNT(*) > 1
        """, nativeQuery = true)
    List<Object[]> findPopularTagCombinations();

    /**
     * 페이지의 태그 삭제
     */
    @Modifying
    @Query("DELETE FROM PageTag pt WHERE pt.page = :page")
    void deleteByPage(@Param("page") Page page);

    /**
     * 태그의 모든 연관관계 삭제
     */
    @Modifying
    @Query("DELETE FROM PageTag pt WHERE pt.tag = :tag")
    void deleteByTag(@Param("tag") Tag tag);

    /**
     * 특정 페이지의 특정 태그 삭제
     */
    @Modifying
    @Query("DELETE FROM PageTag pt WHERE pt.page = :page AND pt.tag = :tag")
    void deleteByPageAndTag(@Param("page") Page page, @Param("tag") Tag tag);

    /**
     * 워크스페이스의 모든 페이지-태그 연관관계 삭제
     */
    @Modifying
    @Query("DELETE FROM PageTag pt WHERE pt.page.workspace = :workspace")
    void deleteByWorkspace(@Param("workspace") Workspace workspace);

    /**
     * 사용자가 만든 페이지-태그 연관관계 삭제
     */
    @Modifying
    @Query("DELETE FROM PageTag pt WHERE pt.createdBy = :user")
    void deleteByCreatedBy(@Param("user") User user);

    /**
     * 페이지의 태그 순서 재정렬
     */
    @Modifying
    @Query("UPDATE PageTag pt SET pt.position = :position WHERE pt.id = :id")
    void updatePosition(@Param("id") Long id, @Param("position") Integer position);

    /**
     * 최근 활동이 있는 태그 조회 (페이지-태그 연관관계 생성 기준)
     */
    @Query("""
        SELECT pt.tag FROM PageTag pt 
        WHERE pt.page.workspace = :workspace 
        GROUP BY pt.tag 
        ORDER BY MAX(pt.createdAt) DESC
        """)
    List<Tag> findRecentlyUsedTags(@Param("workspace") Workspace workspace, Pageable pageable);

    /**
     * 특정 위치에 있는 페이지-태그 연관관계 조회
     */
    @Query("SELECT pt FROM PageTag pt WHERE pt.page = :page AND pt.position = :position")
    Optional<PageTag> findByPageAndPosition(@Param("page") Page page, @Param("position") Integer position);

    /**
     * 페이지의 최대 태그 위치 조회
     */
    @Query("SELECT COALESCE(MAX(pt.position), 0) FROM PageTag pt WHERE pt.page = :page")
    Integer findMaxPositionByPage(@Param("page") Page page);
}