package com.stacknote.back.domain.file.repository;

import com.stacknote.back.domain.file.entity.File;
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
 * 파일 Repository
 */
@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    /**
     * 파일 ID로 활성 파일 조회
     */
    @Query("SELECT f FROM File f WHERE f.id = :id AND f.deletedAt IS NULL")
    Optional<File> findActiveFileById(@Param("id") Long id);

    /**
     * 워크스페이스의 모든 파일 조회
     */
    @Query("SELECT f FROM File f WHERE f.workspace = :workspace AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    List<File> findByWorkspace(@Param("workspace") Workspace workspace);

    /**
     * 워크스페이스의 파일 조회 (페이징)
     */
    @Query("SELECT f FROM File f WHERE f.workspace = :workspace AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    List<File> findByWorkspace(@Param("workspace") Workspace workspace, Pageable pageable);

    /**
     * 특정 페이지에 연결된 파일들 조회
     */
    @Query("SELECT f FROM File f WHERE f.page = :page AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    List<File> findByPage(@Param("page") Page page);

    /**
     * 워크스페이스의 연결되지 않은 파일들 조회 (page가 null인 파일)
     */
    @Query("SELECT f FROM File f WHERE f.workspace = :workspace AND f.page IS NULL AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    List<File> findUnattachedFilesByWorkspace(@Param("workspace") Workspace workspace);

    /**
     * 사용자가 업로드한 파일들 조회
     */
    @Query("SELECT f FROM File f WHERE f.uploadedBy = :user AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    List<File> findByUploadedBy(@Param("user") User user, Pageable pageable);

    /**
     * 파일 타입별 조회
     */
    @Query("SELECT f FROM File f WHERE f.workspace = :workspace AND f.fileType = :fileType AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    List<File> findByWorkspaceAndFileType(@Param("workspace") Workspace workspace, @Param("fileType") File.FileType fileType);

    /**
     * 공개 파일들 조회
     */
    @Query("SELECT f FROM File f WHERE f.workspace = :workspace AND f.isPublic = true AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    List<File> findPublicFilesByWorkspace(@Param("workspace") Workspace workspace);

    /**
     * 파일명으로 검색
     */
    @Query("""
        SELECT f FROM File f 
        WHERE f.workspace = :workspace 
        AND f.deletedAt IS NULL 
        AND LOWER(f.originalName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY f.createdAt DESC
        """)
    List<File> searchByOriginalName(@Param("workspace") Workspace workspace, @Param("keyword") String keyword);

    /**
     * 체크섬으로 중복 파일 조회
     */
    @Query("SELECT f FROM File f WHERE f.workspace = :workspace AND f.checksum = :checksum AND f.deletedAt IS NULL")
    List<File> findByWorkspaceAndChecksum(@Param("workspace") Workspace workspace, @Param("checksum") String checksum);

    /**
     * 저장된 파일명으로 조회
     */
    @Query("SELECT f FROM File f WHERE f.storedName = :storedName AND f.deletedAt IS NULL")
    Optional<File> findByStoredName(@Param("storedName") String storedName);

    /**
     * 파일 경로로 조회
     */
    @Query("SELECT f FROM File f WHERE f.filePath = :filePath AND f.deletedAt IS NULL")
    Optional<File> findByFilePath(@Param("filePath") String filePath);

    /**
     * 최근 업로드된 파일들 조회
     */
    @Query("""
        SELECT f FROM File f 
        WHERE f.workspace = :workspace 
        AND f.deletedAt IS NULL 
        AND f.createdAt >= :since
        ORDER BY f.createdAt DESC
        """)
    List<File> findRecentFilesByWorkspace(@Param("workspace") Workspace workspace, @Param("since") LocalDateTime since, Pageable pageable);

    /**
     * 인기 파일들 조회 (다운로드 수 기준)
     */
    @Query("SELECT f FROM File f WHERE f.workspace = :workspace AND f.deletedAt IS NULL ORDER BY f.downloadCount DESC, f.createdAt DESC")
    List<File> findPopularFilesByWorkspace(@Param("workspace") Workspace workspace, Pageable pageable);

    /**
     * 큰 파일들 조회 (크기 기준)
     */
    @Query("SELECT f FROM File f WHERE f.workspace = :workspace AND f.fileSize > :minSize AND f.deletedAt IS NULL ORDER BY f.fileSize DESC")
    List<File> findLargeFilesByWorkspace(@Param("workspace") Workspace workspace, @Param("minSize") Long minSize, Pageable pageable);

    /**
     * 이미지 파일들만 조회
     */
    @Query("SELECT f FROM File f WHERE f.workspace = :workspace AND f.fileType = 'IMAGE' AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    List<File> findImagesByWorkspace(@Param("workspace") Workspace workspace);

    /**
     * 다운로드 수 증가
     */
    @Modifying
    @Query("UPDATE File f SET f.downloadCount = f.downloadCount + 1 WHERE f.id = :fileId")
    int incrementDownloadCount(@Param("fileId") Long fileId);

    /**
     * 워크스페이스의 파일 개수 조회
     */
    @Query("SELECT COUNT(f) FROM File f WHERE f.workspace = :workspace AND f.deletedAt IS NULL")
    long countByWorkspace(@Param("workspace") Workspace workspace);

    /**
     * 파일 타입별 개수 조회
     */
    @Query("SELECT COUNT(f) FROM File f WHERE f.workspace = :workspace AND f.fileType = :fileType AND f.deletedAt IS NULL")
    long countByWorkspaceAndFileType(@Param("workspace") Workspace workspace, @Param("fileType") File.FileType fileType);

    /**
     * 워크스페이스의 총 파일 크기 조회
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM File f WHERE f.workspace = :workspace AND f.deletedAt IS NULL")
    long getTotalFileSizeByWorkspace(@Param("workspace") Workspace workspace);

    /**
     * 사용자별 파일 크기 합계 조회
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM File f WHERE f.uploadedBy = :user AND f.deletedAt IS NULL")
    long getTotalFileSizeByUser(@Param("user") User user);

    /**
     * 오래된 임시 파일들 조회 (페이지에 연결되지 않은 오래된 파일)
     */
    @Query("""
        SELECT f FROM File f 
        WHERE f.page IS NULL 
        AND f.deletedAt IS NULL 
        AND f.createdAt < :cutoffDate
        ORDER BY f.createdAt
        """)
    List<File> findOldTemporaryFiles(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * 페이지 삭제 시 연결된 파일들의 페이지 참조 제거
     */
    @Modifying
    @Query("UPDATE File f SET f.page = NULL WHERE f.page = :page")
    int detachFilesFromPage(@Param("page") Page page);

    /**
     * 중복 제거를 위한 체크섬별 파일 조회
     */
    @Query("""
        SELECT f.checksum, COUNT(f) as count, MIN(f.id) as firstFileId 
        FROM File f 
        WHERE f.workspace = :workspace 
        AND f.deletedAt IS NULL 
        AND f.checksum IS NOT NULL
        GROUP BY f.checksum 
        HAVING COUNT(f) > 1
        """)
    List<Object[]> findDuplicateFilesByChecksum(@Param("workspace") Workspace workspace);

    /**
     * 미사용 파일들 조회 (어디에도 참조되지 않는 파일)
     */
    @Query("""
        SELECT f FROM File f 
        WHERE f.workspace = :workspace 
        AND f.page IS NULL 
        AND f.deletedAt IS NULL 
        AND f.createdAt < :cutoffDate
        AND NOT EXISTS (
            SELECT 1 FROM Page p 
            WHERE p.content LIKE CONCAT('%', f.fileUrl, '%') 
            AND p.deletedAt IS NULL
        )
        """)
    List<File> findUnusedFiles(@Param("workspace") Workspace workspace, @Param("cutoffDate") LocalDateTime cutoffDate);
}