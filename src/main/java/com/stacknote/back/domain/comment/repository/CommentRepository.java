package com.stacknote.back.domain.comment.repository;

import com.stacknote.back.domain.comment.entity.Comment;
import com.stacknote.back.domain.page.entity.Page;
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
 * 댓글 Repository
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 댓글 ID로 활성 댓글 조회
     */
    @Query("SELECT c FROM Comment c WHERE c.id = :id AND c.deletedAt IS NULL")
    Optional<Comment> findActiveCommentById(@Param("id") Long id);

    /**
     * 페이지의 모든 댓글 조회 (최상위 댓글만, 정렬: 생성일순)
     */
    @Query("SELECT c FROM Comment c WHERE c.page = :page AND c.parent IS NULL AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findRootCommentsByPage(@Param("page") Page page);

    /**
     * 페이지의 모든 댓글 조회 (최상위 댓글만, 페이징)
     */
    @Query("SELECT c FROM Comment c WHERE c.page = :page AND c.parent IS NULL AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findRootCommentsByPage(@Param("page") Page page, Pageable pageable);

    /**
     * 특정 댓글의 대댓글 조회
     */
    @Query("SELECT c FROM Comment c WHERE c.parent = :parent AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParent(@Param("parent") Comment parent);

    /**
     * 페이지의 전체 댓글 수 조회 (대댓글 포함)
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.page = :page AND c.deletedAt IS NULL")
    long countCommentsByPage(@Param("page") Page page);

    /**
     * 페이지의 최상위 댓글 수 조회
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.page = :page AND c.parent IS NULL AND c.deletedAt IS NULL")
    long countRootCommentsByPage(@Param("page") Page page);

    /**
     * 사용자가 작성한 댓글 조회
     */
    @Query("SELECT c FROM Comment c WHERE c.author = :author AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    List<Comment> findCommentsByAuthor(@Param("author") User author, Pageable pageable);

    /**
     * 사용자가 작성한 댓글 수 조회
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.author = :author AND c.deletedAt IS NULL")
    long countCommentsByAuthor(@Param("author") User author);

    /**
     * 특정 기간 내 댓글 조회
     */
    @Query("""
        SELECT c FROM Comment c 
        WHERE c.page = :page 
        AND c.deletedAt IS NULL 
        AND c.createdAt BETWEEN :startDate AND :endDate 
        ORDER BY c.createdAt DESC
        """)
    List<Comment> findCommentsByPageAndDateRange(
            @Param("page") Page page,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 최근 댓글 조회 (전체 페이지 대상)
     */
    @Query("SELECT c FROM Comment c WHERE c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    List<Comment> findRecentComments(Pageable pageable);

    /**
     * 워크스페이스의 최근 댓글 조회
     */
    @Query("""
        SELECT c FROM Comment c 
        WHERE c.page.workspace.id = :workspaceId 
        AND c.deletedAt IS NULL 
        ORDER BY c.createdAt DESC
        """)
    List<Comment> findRecentCommentsByWorkspace(@Param("workspaceId") Long workspaceId, Pageable pageable);

    /**
     * 인기 댓글 조회 (좋아요 수 기준)
     */
    @Query("SELECT c FROM Comment c WHERE c.page = :page AND c.deletedAt IS NULL ORDER BY c.likesCount DESC, c.createdAt DESC")
    List<Comment> findPopularCommentsByPage(@Param("page") Page page, Pageable pageable);

    /**
     * 멘션이 포함된 댓글 조회
     */
    @Query("SELECT c FROM Comment c WHERE c.mentions IS NOT NULL AND c.mentions != '' AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    List<Comment> findCommentsWithMentions(Pageable pageable);

    /**
     * 특정 사용자를 멘션한 댓글 조회
     */
    @Query("SELECT c FROM Comment c WHERE c.mentions LIKE %:username% AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    List<Comment> findCommentsByMentionedUser(@Param("username") String username, Pageable pageable);

    /**
     * 댓글 내용으로 검색
     */
    @Query("""
        SELECT c FROM Comment c 
        WHERE c.page = :page 
        AND c.deletedAt IS NULL 
        AND LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY c.createdAt DESC
        """)
    List<Comment> searchCommentsByContent(@Param("page") Page page, @Param("keyword") String keyword);

    /**
     * 페이지 삭제 시 모든 댓글 소프트 삭제
     */
    @Modifying
    @Query("UPDATE Comment c SET c.deletedAt = CURRENT_TIMESTAMP WHERE c.page = :page AND c.deletedAt IS NULL")
    int softDeleteCommentsByPage(@Param("page") Page page);

    /**
     * 사용자 계정 삭제 시 작성한 모든 댓글 소프트 삭제
     */
    @Modifying
    @Query("UPDATE Comment c SET c.deletedAt = CURRENT_TIMESTAMP WHERE c.author = :author AND c.deletedAt IS NULL")
    int softDeleteCommentsByAuthor(@Param("author") User author);

    /**
     * 댓글의 모든 대댓글 조회 (재귀적)
     */
    @Query(value = """
        WITH RECURSIVE comment_tree AS (
            SELECT id, parent_id, 0 as level
            FROM comments 
            WHERE parent_id = :commentId AND deleted_at IS NULL
            UNION ALL
            SELECT c.id, c.parent_id, ct.level + 1
            FROM comments c
            INNER JOIN comment_tree ct ON c.parent_id = ct.id
            WHERE c.deleted_at IS NULL
        )
        SELECT id FROM comment_tree ORDER BY level, id
        """, nativeQuery = true)
    List<Long> findAllDescendantIds(@Param("commentId") Long commentId);

    /**
     * 오래된 댓글 정리 (관리자용)
     */
    @Query("SELECT c FROM Comment c WHERE c.createdAt < :cutoffDate AND c.deletedAt IS NULL")
    List<Comment> findOldComments(@Param("cutoffDate") LocalDateTime cutoffDate, Pageable pageable);

    /**
     * 좋아요가 많은 댓글 조회 (전체)
     */
    @Query("SELECT c FROM Comment c WHERE c.deletedAt IS NULL AND c.likesCount > :minLikes ORDER BY c.likesCount DESC")
    List<Comment> findPopularComments(@Param("minLikes") int minLikes, Pageable pageable);

    /**
     * 특정 댓글의 경로 조회 (루트부터 해당 댓글까지)
     */
    @Query(value = """
        WITH RECURSIVE comment_path AS (
            SELECT id, parent_id, content, 0 as level
            FROM comments 
            WHERE id = :commentId
            UNION ALL
            SELECT c.id, c.parent_id, c.content, cp.level + 1
            FROM comments c
            INNER JOIN comment_path cp ON c.id = cp.parent_id
            WHERE c.deleted_at IS NULL
        )
        SELECT id FROM comment_path ORDER BY level DESC
        """, nativeQuery = true)
    List<Long> findCommentPath(@Param("commentId") Long commentId);
}