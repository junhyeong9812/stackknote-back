package com.stacknote.back.domain.page.repository;

import com.stacknote.back.domain.page.entity.Page;
import com.stacknote.back.domain.page.entity.PageHistory;
import com.stacknote.back.domain.user.entity.User;
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
 * 페이지 히스토리 Repository
 */
@Repository
public interface PageHistoryRepository extends JpaRepository<PageHistory, Long> {

    /**
     * 특정 페이지의 히스토리 목록 조회 (최신순)
     */
    @Query("SELECT h FROM PageHistory h WHERE h.page = :page ORDER BY h.version DESC")
    List<PageHistory> findByPageOrderByVersionDesc(@Param("page") Page page);

    /**
     * 특정 페이지의 히스토리 목록 조회 (페이징)
     */
    @Query("SELECT h FROM PageHistory h WHERE h.page = :page ORDER BY h.version DESC")
    List<PageHistory> findByPage(@Param("page") Page page, Pageable pageable);

    /**
     * 특정 페이지의 특정 버전 조회
     */
    @Query("SELECT h FROM PageHistory h WHERE h.page = :page AND h.version = :version")
    Optional<PageHistory> findByPageAndVersion(@Param("page") Page page, @Param("version") Integer version);

    /**
     * 특정 페이지의 최신 버전 번호 조회
     */
    @Query("SELECT COALESCE(MAX(h.version), 0) FROM PageHistory h WHERE h.page = :page")
    int findMaxVersionByPage(@Param("page") Page page);

    /**
     * 특정 페이지의 히스토리 개수 조회
     */
    @Query("SELECT COUNT(h) FROM PageHistory h WHERE h.page = :page")
    long countByPage(@Param("page") Page page);

    /**
     * 특정 사용자가 수정한 히스토리 목록 조회
     */
    @Query("SELECT h FROM PageHistory h WHERE h.modifiedBy = :user ORDER BY h.createdAt DESC")
    List<PageHistory> findByModifiedByOrderByCreatedAtDesc(@Param("user") User user, Pageable pageable);

    /**
     * 특정 기간 내의 페이지 히스토리 조회
     */
    @Query("""
        SELECT h FROM PageHistory h 
        WHERE h.page = :page 
        AND h.createdAt BETWEEN :startDate AND :endDate 
        ORDER BY h.version DESC
        """)
    List<PageHistory> findByPageAndCreatedAtBetween(
            @Param("page") Page page,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 특정 변경 유형의 히스토리 조회
     */
    @Query("SELECT h FROM PageHistory h WHERE h.page = :page AND h.changeType = :changeType ORDER BY h.version DESC")
    List<PageHistory> findByPageAndChangeType(@Param("page") Page page, @Param("changeType") PageHistory.ChangeType changeType);

    /**
     * 콘텐츠 변경이 있었던 히스토리만 조회
     */
    @Query("""
        SELECT h FROM PageHistory h 
        WHERE h.page = :page 
        AND h.changeType IN ('CONTENT_UPDATED', 'TITLE_UPDATED', 'MAJOR_UPDATE', 'CREATED') 
        ORDER BY h.version DESC
        """)
    List<PageHistory> findContentChangesHistory(@Param("page") Page page);

    /**
     * 특정 페이지의 이전 버전 조회 (현재 버전 기준)
     */
    @Query("SELECT h FROM PageHistory h WHERE h.page = :page AND h.version < :currentVersion ORDER BY h.version DESC")
    List<PageHistory> findPreviousVersions(@Param("page") Page page, @Param("currentVersion") Integer currentVersion, Pageable pageable);

    /**
     * 특정 페이지의 다음 버전 조회 (현재 버전 기준)
     */
    @Query("SELECT h FROM PageHistory h WHERE h.page = :page AND h.version > :currentVersion ORDER BY h.version ASC")
    List<PageHistory> findNextVersions(@Param("page") Page page, @Param("currentVersion") Integer currentVersion, Pageable pageable);

    /**
     * 특정 워크스페이스의 최근 히스토리 조회
     */
    @Query("""
        SELECT h FROM PageHistory h 
        WHERE h.page.workspace.id = :workspaceId 
        ORDER BY h.createdAt DESC
        """)
    List<PageHistory> findRecentHistoryByWorkspace(@Param("workspaceId") Long workspaceId, Pageable pageable);

    /**
     * 오래된 히스토리 삭제 (정리 작업용)
     */
    @Modifying
    @Query("DELETE FROM PageHistory h WHERE h.createdAt < :cutoffDate")
    int deleteOldHistory(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * 특정 페이지의 오래된 히스토리 삭제 (최신 N개 버전 제외)
     */
    @Modifying
    @Query("""
        DELETE FROM PageHistory h 
        WHERE h.page = :page 
        AND h.version NOT IN (
            SELECT h2.version FROM PageHistory h2 
            WHERE h2.page = :page 
            ORDER BY h2.version DESC 
            LIMIT :keepCount
        )
        """)
    int deleteOldHistoryKeepLatest(@Param("page") Page page, @Param("keepCount") int keepCount);

    /**
     * 페이지 삭제 시 모든 히스토리 삭제
     */
    @Modifying
    @Query("DELETE FROM PageHistory h WHERE h.page = :page")
    int deleteAllByPage(@Param("page") Page page);

    /**
     * 특정 기간의 히스토리 통계 조회 (변경 횟수)
     */
    @Query("""
        SELECT COUNT(h) FROM PageHistory h 
        WHERE h.page = :page 
        AND h.createdAt BETWEEN :startDate AND :endDate
        """)
    long countHistoryInPeriod(@Param("page") Page page, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 특정 사용자의 페이지별 기여도 조회
     */
    @Query("""
        SELECT h.page.id, COUNT(h) 
        FROM PageHistory h 
        WHERE h.modifiedBy = :user 
        AND h.page.workspace.id = :workspaceId
        GROUP BY h.page.id 
        ORDER BY COUNT(h) DESC
        """)
    List<Object[]> findUserContributionByWorkspace(@Param("user") User user, @Param("workspaceId") Long workspaceId);
}