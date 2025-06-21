package com.stacknote.back.domain.file.service.query;

import com.stacknote.back.domain.file.dto.response.FileResponse;
import com.stacknote.back.domain.file.entity.File;
import com.stacknote.back.domain.file.exception.FileNotFoundException;
import com.stacknote.back.domain.file.repository.FileRepository;
import com.stacknote.back.domain.page.entity.Page;
import com.stacknote.back.domain.page.exception.PageNotFoundException;
import com.stacknote.back.domain.page.repository.PageRepository;
import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.domain.workspace.entity.Workspace;
import com.stacknote.back.domain.workspace.exception.WorkspaceAccessDeniedException;
import com.stacknote.back.domain.workspace.exception.WorkspaceNotFoundException;
import com.stacknote.back.domain.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 파일 관련 쿼리 서비스
 * 파일 조회, 검색 등의 읽기 전용 작업 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileQueryService {

    private final FileRepository fileRepository;
    private final WorkspaceRepository workspaceRepository;
    private final PageRepository pageRepository;

    /**
     * 파일 상세 조회
     */
    @Transactional // 다운로드 수 증가를 위해 쓰기 트랜잭션 필요
    public FileResponse getFile(Long fileId, User currentUser) {
        log.debug("파일 상세 조회: {}, 사용자: {}", fileId, currentUser.getId());

        File file = getAccessibleFile(fileId, currentUser);

        // 다운로드 수 증가
        fileRepository.incrementDownloadCount(fileId);

        return FileResponse.from(file);
    }

    /**
     * 워크스페이스의 모든 파일 목록 조회
     */
    public List<FileResponse> getWorkspaceFiles(Long workspaceId, User currentUser) {
        log.debug("워크스페이스 파일 목록 조회: {}, 사용자: {}", workspaceId, currentUser.getId());

        Workspace workspace = getAccessibleWorkspace(workspaceId, currentUser);
        List<File> files = fileRepository.findByWorkspace(workspace);

        return files.stream()
                .map(FileResponse::fromSummary)
                .collect(Collectors.toList());
    }

    /**
     * 워크스페이스의 파일 목록 조회 (페이징)
     */
    public List<FileResponse> getWorkspaceFiles(Long workspaceId, User currentUser, int page, int size) {
        log.debug("워크스페이스 파일 목록 조회 (페이징): {}, 사용자: {}", workspaceId, currentUser.getId());

        Workspace workspace = getAccessibleWorkspace(workspaceId, currentUser);
        Pageable pageable = PageRequest.of(page, size);
        List<File> files = fileRepository.findByWorkspace(workspace, pageable);

        return files.stream()
                .map(FileResponse::fromSummary)
                .collect(Collectors.toList());
    }

    /**
     * 특정 페이지의 파일들 조회
     */
    public List<FileResponse> getPageFiles(Long pageId, User currentUser) {
        log.debug("페이지 파일 목록 조회: {}, 사용자: {}", pageId, currentUser.getId());

        Page page = getAccessiblePage(pageId, currentUser);
        List<File> files = fileRepository.findByPage(page);

        return files.stream()
                .map(FileResponse::fromSummary)
                .collect(Collectors.toList());
    }

    /**
     * 워크스페이스의 연결되지 않은 파일들 조회
     */
    public List<FileResponse> getUnattachedFiles(Long workspaceId, User currentUser) {
        log.debug("연결되지 않은 파일 목록 조회: {}, 사용자: {}", workspaceId, currentUser.getId());

        Workspace workspace = getAccessibleWorkspace(workspaceId, currentUser);
        List<File> files = fileRepository.findUnattachedFilesByWorkspace(workspace);

        return files.stream()
                .map(FileResponse::fromSummary)
                .collect(Collectors.toList());
    }

    /**
     * 파일 타입별 조회
     */
    public List<FileResponse> getFilesByType(Long workspaceId, File.FileType fileType, User currentUser) {
        log.debug("파일 타입별 조회: {}, 타입: {}, 사용자: {}", workspaceId, fileType, currentUser.getId());

        Workspace workspace = getAccessibleWorkspace(workspaceId, currentUser);
        List<File> files = fileRepository.findByWorkspaceAndFileType(workspace, fileType);

        return files.stream()
                .map(FileResponse::fromSummary)
                .collect(Collectors.toList());
    }

    /**
     * 이미지 파일들만 조회
     */
    public List<FileResponse> getImageFiles(Long workspaceId, User currentUser) {
        log.debug("이미지 파일 목록 조회: {}, 사용자: {}", workspaceId, currentUser.getId());

        Workspace workspace = getAccessibleWorkspace(workspaceId, currentUser);
        List<File> files = fileRepository.findImagesByWorkspace(workspace);

        return files.stream()
                .map(FileResponse::fromSummary)
                .collect(Collectors.toList());
    }

    /**
     * 파일 검색
     */
    public List<FileResponse> searchFiles(Long workspaceId, String keyword, User currentUser) {
        log.debug("파일 검색: 워크스페이스: {}, 키워드: {}, 사용자: {}", workspaceId, keyword, currentUser.getId());

        Workspace workspace = getAccessibleWorkspace(workspaceId, currentUser);
        List<File> files = fileRepository.searchByOriginalName(workspace, keyword);

        return files.stream()
                .map(FileResponse::fromSummary)
                .collect(Collectors.toList());
    }

    /**
     * 최근 업로드된 파일들 조회
     */
    public List<FileResponse> getRecentFiles(Long workspaceId, User currentUser, int days, int limit) {
        log.debug("최근 업로드된 파일 조회: 워크스페이스: {}, 사용자: {}", workspaceId, currentUser.getId());

        Workspace workspace = getAccessibleWorkspace(workspaceId, currentUser);
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        Pageable pageable = PageRequest.of(0, limit);

        List<File> files = fileRepository.findRecentFilesByWorkspace(workspace, since, pageable);

        return files.stream()
                .map(FileResponse::fromSummary)
                .collect(Collectors.toList());
    }

    /**
     * 인기 파일들 조회 (다운로드 수 기준)
     */
    public List<FileResponse> getPopularFiles(Long workspaceId, User currentUser, int limit) {
        log.debug("인기 파일 목록 조회: 워크스페이스: {}, 사용자: {}", workspaceId, currentUser.getId());

        Workspace workspace = getAccessibleWorkspace(workspaceId, currentUser);
        Pageable pageable = PageRequest.of(0, limit);

        List<File> files = fileRepository.findPopularFilesByWorkspace(workspace, pageable);

        return files.stream()
                .map(FileResponse::fromSummary)
                .collect(Collectors.toList());
    }

    /**
     * 사용자가 업로드한 파일들 조회
     */
    public List<FileResponse> getUserUploadedFiles(User user, int limit) {
        log.debug("사용자 업로드 파일 목록 조회: {}", user.getId());

        Pageable pageable = PageRequest.of(0, limit);
        List<File> files = fileRepository.findByUploadedBy(user, pageable);

        return files.stream()
                .map(FileResponse::fromSummary)
                .collect(Collectors.toList());
    }

    /**
     * 워크스페이스 파일 통계 조회
     */
    public FileStatisticsResponse getWorkspaceFileStatistics(Long workspaceId, User currentUser) {
        log.debug("워크스페이스 파일 통계 조회: {}, 사용자: {}", workspaceId, currentUser.getId());

        Workspace workspace = getAccessibleWorkspace(workspaceId, currentUser);

        long totalFiles = fileRepository.countByWorkspace(workspace);
        long totalSize = fileRepository.getTotalFileSizeByWorkspace(workspace);
        long imageCount = fileRepository.countByWorkspaceAndFileType(workspace, File.FileType.IMAGE);
        long documentCount = fileRepository.countByWorkspaceAndFileType(workspace, File.FileType.DOCUMENT);
        long videoCount = fileRepository.countByWorkspaceAndFileType(workspace, File.FileType.VIDEO);
        long audioCount = fileRepository.countByWorkspaceAndFileType(workspace, File.FileType.AUDIO);
        long otherCount = totalFiles - imageCount - documentCount - videoCount - audioCount;

        return new FileStatisticsResponse(
                totalFiles, totalSize, imageCount, documentCount,
                videoCount, audioCount, otherCount
        );
    }

    // ===== 내부 헬퍼 메서드 =====

    private Workspace getAccessibleWorkspace(Long workspaceId, User user) {
        Workspace workspace = workspaceRepository.findActiveWorkspaceById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException("워크스페이스를 찾을 수 없습니다."));

        if (!canUserAccessWorkspace(workspace, user)) {
            throw new WorkspaceAccessDeniedException("워크스페이스에 접근할 권한이 없습니다.");
        }

        return workspace;
    }

    private Page getAccessiblePage(Long pageId, User user) {
        Page page = pageRepository.findActivePageById(pageId)
                .orElseThrow(() -> new PageNotFoundException("페이지를 찾을 수 없습니다."));

        if (!canUserAccessWorkspace(page.getWorkspace(), user)) {
            throw new WorkspaceAccessDeniedException("페이지에 접근할 권한이 없습니다.");
        }

        return page;
    }

    private File getAccessibleFile(Long fileId, User user) {
        File file = fileRepository.findActiveFileById(fileId)
                .orElseThrow(() -> new FileNotFoundException("파일을 찾을 수 없습니다."));

        if (!canUserAccessWorkspace(file.getWorkspace(), user)) {
            throw new WorkspaceAccessDeniedException("파일에 접근할 권한이 없습니다.");
        }

        return file;
    }

    private boolean canUserAccessWorkspace(Workspace workspace, User user) {
        return workspace.isOwner(user) ||
                workspace.isMember(user) ||
                workspace.getVisibility() == Workspace.Visibility.PUBLIC;
    }

    /**
     * 파일 통계 응답 DTO
     */
    public static class FileStatisticsResponse {
        private final long totalFiles;
        private final long totalSize;
        private final long imageCount;
        private final long documentCount;
        private final long videoCount;
        private final long audioCount;
        private final long otherCount;

        public FileStatisticsResponse(long totalFiles, long totalSize, long imageCount,
                                      long documentCount, long videoCount, long audioCount, long otherCount) {
            this.totalFiles = totalFiles;
            this.totalSize = totalSize;
            this.imageCount = imageCount;
            this.documentCount = documentCount;
            this.videoCount = videoCount;
            this.audioCount = audioCount;
            this.otherCount = otherCount;
        }

        // Getters
        public long getTotalFiles() { return totalFiles; }
        public long getTotalSize() { return totalSize; }
        public long getImageCount() { return imageCount; }
        public long getDocumentCount() { return documentCount; }
        public long getVideoCount() { return videoCount; }
        public long getAudioCount() { return audioCount; }
        public long getOtherCount() { return otherCount; }

        public String getFormattedTotalSize() {
            if (totalSize < 1024) return totalSize + " B";
            int exp = (int) (Math.log(totalSize) / Math.log(1024));
            String pre = "KMGTPE".charAt(exp - 1) + "";
            return String.format("%.1f %sB", totalSize / Math.pow(1024, exp), pre);
        }
    }
}