package com.stacknote.back.domain.file.service.command;

import com.stacknote.back.domain.file.dto.request.FileUpdateRequest;
import com.stacknote.back.domain.file.dto.request.FileUploadRequest;
import com.stacknote.back.domain.file.dto.response.FileResponse;
import com.stacknote.back.domain.file.dto.response.FileUploadResponse;
import com.stacknote.back.domain.file.entity.File;
import com.stacknote.back.domain.file.exception.FileNotFoundException;
import com.stacknote.back.domain.file.exception.FileStorageException;
import com.stacknote.back.domain.file.exception.FileUploadException;
import com.stacknote.back.domain.file.exception.FileSizeExceededException;
import com.stacknote.back.domain.file.exception.InvalidFileTypeException;
import com.stacknote.back.domain.file.repository.FileRepository;
import com.stacknote.back.domain.file.service.FileStorageService;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 파일 관련 명령 서비스
 * 파일 업로드, 수정, 삭제 등의 명령 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FileCommandService {

    private final FileRepository fileRepository;
    private final WorkspaceRepository workspaceRepository;
    private final PageRepository pageRepository;
    private final FileStorageService fileStorageService;

    @Value("${file.max-size:52428800}") // 기본 50MB
    private long maxFileSize;

    /**
     * 파일 업로드
     */
    public FileUploadResponse uploadFile(Long workspaceId, User currentUser,
                                         MultipartFile multipartFile, FileUploadRequest request) {
        log.info("파일 업로드 시도: {}, 워크스페이스: {}, 사용자: {}",
                multipartFile.getOriginalFilename(), workspaceId, currentUser.getId());

        // 1. 파일 유효성 검증
        validateFile(multipartFile);

        // 2. 워크스페이스 권한 확인
        Workspace workspace = getWorkspaceWithWritePermission(workspaceId, currentUser);

        // 3. 페이지 확인 (선택사항)
        Page page = validateAndGetPage(request.getPageId(), workspaceId);

        try {
            // 4. 파일 메타데이터 추출
            String originalName = multipartFile.getOriginalFilename();
            String mimeType = multipartFile.getContentType();
            long fileSize = multipartFile.getSize();
            File.FileType fileType = determineFileType(mimeType, originalName);

            // 5. 중복 파일 체크
            String checksum = calculateChecksum(multipartFile.getBytes());
            File existingFile = findDuplicateFile(workspace, checksum);
            if (existingFile != null) {
                log.info("중복 파일 발견: {}, 기존 파일 반환", originalName);
                return FileUploadResponse.from(existingFile);
            }

            // 6. 파일 저장 경로 생성
            String storedName = generateStoredFileName(originalName);
            String filePath = generateFilePath(storedName);
            String fileUrl = "/api" + filePath;

            // 7. 실제 파일 저장
            fileStorageService.storeFile(multipartFile, filePath);

            // 8. 파일 엔티티 생성 및 저장
            File file = createFileEntity(
                    originalName, storedName, filePath, fileUrl, fileSize,
                    mimeType, fileType, checksum, workspace, page, currentUser, request
            );

            File savedFile = fileRepository.save(file);

            log.info("파일 업로드 완료: {}", savedFile.getId());
            return FileUploadResponse.from(savedFile);

        } catch (IOException e) {
            log.error("파일 저장 중 오류: {}", e.getMessage(), e);
            throw new FileStorageException("파일 저장에 실패했습니다.");
        } catch (Exception e) {
            log.error("파일 업로드 중 오류: {}", e.getMessage(), e);
            throw new FileUploadException("파일 업로드에 실패했습니다.");
        }
    }

    /**
     * 파일 정보 수정
     */
    public FileResponse updateFile(Long fileId, User currentUser, FileUpdateRequest request) {
        log.info("파일 정보 수정 시도: {}, 사용자: {}", fileId, currentUser.getId());

        File file = getFileWithPermission(fileId, currentUser, true);

        // 페이지 연결 변경
        updateFilePageConnection(file, request.getPageId());

        // 기본 정보 수정
        file.updateInfo(request.getDescription(), request.getIsPublic());

        File updatedFile = fileRepository.save(file);

        log.info("파일 정보 수정 완료: {}", fileId);
        return FileResponse.from(updatedFile);
    }

    /**
     * 파일 삭제
     */
    public void deleteFile(Long fileId, User currentUser) {
        log.info("파일 삭제 시도: {}, 사용자: {}", fileId, currentUser.getId());

        File file = getFileWithPermission(fileId, currentUser, true);

        try {
            // 실제 파일 삭제
            fileStorageService.deleteFile(file.getFilePath());

            // 썸네일 삭제 (있는 경우)
            if (file.hasThumbnail()) {
                fileStorageService.deleteFile(file.getThumbnailUrl());
            }

            // 소프트 삭제
            file.markAsDeleted();
            fileRepository.save(file);

            log.info("파일 삭제 완료: {}", fileId);

        } catch (Exception e) {
            log.error("파일 삭제 중 오류: {}", e.getMessage(), e);
            throw new FileStorageException("파일 삭제에 실패했습니다.");
        }
    }

    /**
     * 파일 공개 상태 토글
     */
    public FileResponse toggleFileVisibility(Long fileId, User currentUser) {
        log.info("파일 공개 상태 변경: {}, 사용자: {}", fileId, currentUser.getId());

        File file = getFileWithPermission(fileId, currentUser, true);
        file.updateInfo(null, !file.getIsPublic());

        File updatedFile = fileRepository.save(file);
        return FileResponse.from(updatedFile);
    }

    /**
     * 페이지에서 파일 연결 해제
     */
    public FileResponse detachFileFromPage(Long fileId, User currentUser) {
        log.info("파일 페이지 연결 해제: {}, 사용자: {}", fileId, currentUser.getId());

        File file = getFileWithPermission(fileId, currentUser, true);
        file.detachFromPage();

        File updatedFile = fileRepository.save(file);
        return FileResponse.from(updatedFile);
    }

    // ===== 내부 헬퍼 메서드 =====

    /**
     * 파일 유효성 검증
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileUploadException("파일이 비어있습니다.");
        }

        if (file.getSize() > maxFileSize) {
            throw new FileSizeExceededException(
                    "파일 크기가 너무 큽니다. 최대 " + (maxFileSize / 1024 / 1024) + "MB까지 업로드 가능합니다."
            );
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.trim().isEmpty()) {
            throw new FileUploadException("파일명이 유효하지 않습니다.");
        }

        // 위험한 파일 확장자 차단
        String extension = getFileExtension(originalName).toLowerCase();
        List<String> blockedExtensions = List.of("exe", "bat", "sh", "cmd", "scr", "msi", "dll");
        if (blockedExtensions.contains(extension)) {
            throw new InvalidFileTypeException("지원하지 않는 파일 형식입니다.");
        }
    }

    /**
     * 페이지 유효성 검증 및 조회
     */
    private Page validateAndGetPage(Long pageId, Long workspaceId) {
        if (pageId == null) {
            return null;
        }

        Page page = pageRepository.findActivePageById(pageId)
                .orElseThrow(() -> new PageNotFoundException("페이지를 찾을 수 없습니다."));

        if (!page.getWorkspace().getId().equals(workspaceId)) {
            throw new IllegalArgumentException("페이지가 다른 워크스페이스에 속해 있습니다.");
        }

        return page;
    }

    /**
     * 중복 파일 검색
     */
    private File findDuplicateFile(Workspace workspace, String checksum) {
        List<File> duplicates = fileRepository.findByWorkspaceAndChecksum(workspace, checksum);
        return duplicates.isEmpty() ? null : duplicates.get(0);
    }

    /**
     * 파일 타입 결정
     */
    private File.FileType determineFileType(String mimeType, String fileName) {
        // MIME 타입 기반 판단
        if (mimeType != null) {
            if (mimeType.startsWith("image/")) return File.FileType.IMAGE;
            if (mimeType.startsWith("video/")) return File.FileType.VIDEO;
            if (mimeType.startsWith("audio/")) return File.FileType.AUDIO;
            if (mimeType.equals("application/pdf") ||
                    mimeType.contains("document") ||
                    mimeType.contains("sheet") ||
                    mimeType.contains("presentation") ||
                    mimeType.startsWith("text/")) {
                return File.FileType.DOCUMENT;
            }
            if (mimeType.contains("zip") || mimeType.contains("archive")) {
                return File.FileType.ARCHIVE;
            }
        }

        // 확장자 기반 판단
        String extension = getFileExtension(fileName).toLowerCase();

        if (List.of("jpg", "jpeg", "png", "gif", "svg", "webp", "bmp").contains(extension)) {
            return File.FileType.IMAGE;
        }
        if (List.of("mp4", "avi", "mov", "wmv", "flv", "webm", "mkv").contains(extension)) {
            return File.FileType.VIDEO;
        }
        if (List.of("mp3", "wav", "flac", "aac", "ogg", "wma").contains(extension)) {
            return File.FileType.AUDIO;
        }
        if (List.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "rtf", "md").contains(extension)) {
            return File.FileType.DOCUMENT;
        }
        if (List.of("zip", "rar", "7z", "tar", "gz", "bz2").contains(extension)) {
            return File.FileType.ARCHIVE;
        }
        if (List.of("js", "java", "py", "cpp", "c", "html", "css", "xml", "json", "sql").contains(extension)) {
            return File.FileType.CODE;
        }

        return File.FileType.OTHER;
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * 저장용 파일명 생성
     */
    private String generateStoredFileName(String originalName) {
        String extension = getFileExtension(originalName);
        String uuid = UUID.randomUUID().toString();
        return extension.isEmpty() ? uuid : uuid + "." + extension;
    }

    /**
     * 파일 저장 경로 생성 (/files/yyyy/mm/filename)
     */
    private String generateFilePath(String storedName) {
        LocalDateTime now = LocalDateTime.now();
        return String.format("/files/%d/%02d/%s",
                now.getYear(), now.getMonthValue(), storedName);
    }

    /**
     * 체크섬 계산
     */
    private String calculateChecksum(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("체크섬 계산 중 오류", e);
            return null;
        }
    }

    /**
     * 파일 엔티티 생성
     */
    private File createFileEntity(String originalName, String storedName, String filePath, String fileUrl,
                                  long fileSize, String mimeType, File.FileType fileType, String checksum,
                                  Workspace workspace, Page page, User currentUser, FileUploadRequest request) {
        return File.builder()
                .originalName(originalName)
                .storedName(storedName)
                .filePath(filePath)
                .fileUrl(fileUrl)
                .fileSize(fileSize)
                .mimeType(mimeType)
                .fileType(fileType)
                .checksum(checksum)
                .workspace(workspace)
                .page(page)
                .uploadedBy(currentUser)
                .isPublic(request.getIsPublic())
                .description(request.getDescription())
                .build();
    }

    /**
     * 파일 페이지 연결 업데이트
     */
    private void updateFilePageConnection(File file, Long newPageId) {
        if (newPageId != null) {
            Page page = pageRepository.findActivePageById(newPageId)
                    .orElseThrow(() -> new PageNotFoundException("페이지를 찾을 수 없습니다."));

            if (!page.getWorkspace().getId().equals(file.getWorkspace().getId())) {
                throw new IllegalArgumentException("페이지가 다른 워크스페이스에 속해 있습니다.");
            }

            file.attachToPage(page);
        } else {
            file.detachFromPage();
        }
    }

    /**
     * 워크스페이스 쓰기 권한 확인
     */
    private Workspace getWorkspaceWithWritePermission(Long workspaceId, User user) {
        Workspace workspace = workspaceRepository.findActiveWorkspaceById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException("워크스페이스를 찾을 수 없습니다."));

        if (!workspace.isOwner(user) && !workspace.isMember(user)) {
            throw new WorkspaceAccessDeniedException("워크스페이스에 접근할 권한이 없습니다.");
        }

        return workspace;
    }

    /**
     * 파일 접근 권한 확인
     */
    private File getFileWithPermission(Long fileId, User user, boolean requireWritePermission) {
        File file = fileRepository.findActiveFileById(fileId)
                .orElseThrow(() -> new FileNotFoundException("파일을 찾을 수 없습니다."));

        Workspace workspace = file.getWorkspace();

        // 읽기 권한 확인
        if (!workspace.isOwner(user) && !workspace.isMember(user)) {
            throw new WorkspaceAccessDeniedException("파일에 접근할 권한이 없습니다.");
        }

        // 쓰기 권한 확인 (파일 업로더 또는 워크스페이스 소유자만 가능)
        if (requireWritePermission) {
            boolean isFileOwner = file.getUploadedBy().getId().equals(user.getId());
            boolean isWorkspaceOwner = workspace.isOwner(user);

            if (!isFileOwner && !isWorkspaceOwner) {
                throw new WorkspaceAccessDeniedException("파일을 수정할 권한이 없습니다.");
            }
        }

        return file;
    }
}