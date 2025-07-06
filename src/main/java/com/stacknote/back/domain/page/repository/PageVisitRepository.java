package com.stacknote.back.domain.page.repository;

import com.stacknote.back.domain.page.entity.Page;
import com.stacknote.back.domain.page.entity.PageVisit;
import com.stacknote.back.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 페이지 방문 기록 Repository
 */
@Repository
public interface PageVisitRepository extends JpaRepository<PageVisit, Long> {

    /**
     * 사용자와 페이지로 방문 기록 조회
     */
    @Query("SELECT v FROM PageVisit v WHERE v.user = :user AND v.page = :page")
    Optional<PageVisit> findByUserAndPage(@Param("user") User user, @Param("page") Page page);

    /**
     * 사용자의 최근 방문 페이지 조회
     */
    @Query("""
        SELECT v FROM PageVisit v 
        JOIN FETCH v.page p 
        JOIN FETCH p.workspace w
        WHERE v.user = :user 
        AND p.deletedAt IS NULL 
        ORDER BY v.visitedAt DESC
        """)
    List<PageVisit> findRecentVisitsByUser(@Param("user") User user, Pageable pageable);

    /**
     * 특정 기간 이전의 방문 기록 삭제
     */
    @Query("DELETE FROM PageVisit v WHERE v.visitedAt < :cutoffDate")
    int deleteOldVisits(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * 워크스페이스별 최근 방문 페이지 조회
     */
    @Query("""
        SELECT v FROM PageVisit v 
        JOIN FETCH v.page p 
        WHERE v.user = :user 
        AND p.workspace.id = :workspaceId 
        AND p.deletedAt IS NULL 
        ORDER BY v.visitedAt DESC
        """)
    List<PageVisit> findRecentVisitsByUserAndWorkspace(
            @Param("user") User user,
            @Param("workspaceId") Long workspaceId,
            Pageable pageable
    );
}