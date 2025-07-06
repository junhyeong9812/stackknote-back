package com.stacknote.back.domain.page.repository;

import com.stacknote.back.domain.page.entity.Page;
import com.stacknote.back.domain.page.entity.PageFavorite;
import com.stacknote.back.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 페이지 즐겨찾기 Repository
 */
@Repository
public interface PageFavoriteRepository extends JpaRepository<PageFavorite, Long> {

    /**
     * 사용자와 페이지로 즐겨찾기 조회
     */
    @Query("SELECT f FROM PageFavorite f WHERE f.user = :user AND f.page = :page")
    Optional<PageFavorite> findByUserAndPage(@Param("user") User user, @Param("page") Page page);

    /**
     * 사용자의 즐겨찾기 페이지 목록 조회
     */
    @Query("""
        SELECT f FROM PageFavorite f 
        JOIN FETCH f.page p 
        JOIN FETCH p.workspace w
        WHERE f.user = :user 
        AND p.deletedAt IS NULL 
        ORDER BY f.createdAt DESC
        """)
    List<PageFavorite> findByUser(@Param("user") User user);

    /**
     * 워크스페이스별 즐겨찾기 페이지 조회
     */
    @Query("""
        SELECT f FROM PageFavorite f 
        JOIN FETCH f.page p 
        WHERE f.user = :user 
        AND p.workspace.id = :workspaceId 
        AND p.deletedAt IS NULL 
        ORDER BY f.createdAt DESC
        """)
    List<PageFavorite> findByUserAndWorkspace(
            @Param("user") User user,
            @Param("workspaceId") Long workspaceId
    );

    /**
     * 즐겨찾기 여부 확인
     */
    @Query("SELECT COUNT(f) > 0 FROM PageFavorite f WHERE f.user = :user AND f.page = :page")
    boolean existsByUserAndPage(@Param("user") User user, @Param("page") Page page);

    /**
     * 사용자와 페이지로 즐겨찾기 삭제
     */
    void deleteByUserAndPage(User user, Page page);
}