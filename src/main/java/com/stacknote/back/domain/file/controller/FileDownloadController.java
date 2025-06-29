package com.stacknote.back.domain.file.controller;

import com.stacknote.back.domain.file.entity.File;
import com.stacknote.back.domain.file.exception.FileNotFoundException;
import com.stacknote.back.domain.file.repository.FileRepository;
import com.stacknote.back.domain.file.service.FileStorageService;
import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.domain.workspace.exception.WorkspaceAccessDeniedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 파일 다운로드 컨트롤러
 * 파일 다운로드 및 스트리밍 기능 제공
 */
@Slf4j
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(name = "File Download", description = "파일 다운로드 API")
public class FileDownloadController {

    private final FileRepository fileRepository;
    private final FileStorageService fileStorageService;

    /**
     * 파일 다운로드
     */
    @GetMapping("/{fileId}/download")
    @Operation(summary = "파일 다운로드", description = "파일을 다운로드합니다.")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "파일 ID") @PathVariable Long fileId,
            @Parameter(description = "첨부파일로 다운로드 여부") @RequestParam(defaultValue = "true") boolean attachment,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("파일 다운로드 요청: {}, 사용자: {}", fileId, currentUser != null ? currentUser.getId() : "익명");

        // 파일 조회 및 권한 확인
        File file = getAccessibleFile(fileId, currentUser);

        try {
            // 파일 리소스 로드
            Resource resource = fileStorageService.loadFileAsResource(file.getFilePath());

            // 다운로드 수 증가
            fileRepository.incrementDownloadCount(fileId);

            // Content-Type 결정
            String contentType = determineContentType(file);

            // 파일명 인코딩 (한글 파일명 지원)
            String encodedFileName = URLEncoder.encode(file.getOriginalName(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            // 응답 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));

            if (attachment) {
                headers.setContentDispositionFormData("attachment", encodedFileName);
            } else {
                headers.setContentDispositionFormData("inline", encodedFileName);
            }

            // 캐시 제어 헤더
            if (file.getIsPublic()) {
                headers.setCacheControl("public, max-age=3600"); // 1시간 캐시
            } else {
                headers.setCacheControl("private, no-cache");
            }

            log.info("파일 다운로드 완료: {}", fileId);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (Exception e) {
            log.error("파일 다운로드 중 오류: {}", e.getMessage(), e);
            throw new FileNotFoundException("파일을 다운로드할 수 없습니다.");
        }
    }

    /**
     * 이미지 파일 미리보기 (썸네일)
     */
    @GetMapping("/{fileId}/thumbnail")
    @Operation(summary = "이미지 썸네일", description = "이미지 파일의 썸네일을 조회합니다.")
    public ResponseEntity<Resource> getThumbnail(
            @Parameter(description = "파일 ID") @PathVariable Long fileId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("썸네일 조회 요청: {}, 사용자: {}", fileId, currentUser != null ? currentUser.getId() : "익명");

        File file = getAccessibleFile(fileId, currentUser);

        // 이미지 파일이 아니거나 썸네일이 없는 경우
        if (!file.isImage() || !file.hasThumbnail()) {
            throw new FileNotFoundException("썸네일을 찾을 수 없습니다.");
        }

        try {
            Resource resource = fileStorageService.loadFileAsResource(file.getThumbnailUrl());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(file.getMimeType()));
            headers.setCacheControl("public, max-age=86400"); // 24시간 캐시

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (Exception e) {
            log.error("썸네일 로드 중 오류: {}", e.getMessage(), e);
            throw new FileNotFoundException("썸네일을 로드할 수 없습니다.");
        }
    }

    /**
     * 파일 미리보기 (브라우저에서 직접 표시)
     */
    @GetMapping("/{fileId}/preview")
    @Operation(summary = "파일 미리보기", description = "파일을 브라우저에서 직접 미리봅니다.")
    public ResponseEntity<Resource> previewFile(
            @Parameter(description = "파일 ID") @PathVariable Long fileId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("파일 미리보기 요청: {}, 사용자: {}", fileId, currentUser != null ? currentUser.getId() : "익명");

        File file = getAccessibleFile(fileId, currentUser);

        // 미리보기 불가능한 파일
        if (!file.isPreviewable()) {
            throw new IllegalArgumentException("미리보기할 수 없는 파일 형식입니다.");
        }

        try {
            Resource resource = fileStorageService.loadFileAsResource(file.getFilePath());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(file.getMimeType()));
            headers.setContentDispositionFormData("inline", file.getOriginalName());

            // 공개 파일인 경우 캐시 허용
            if (file.getIsPublic()) {
                headers.setCacheControl("public, max-age=3600");
            } else {
                headers.setCacheControl("private, no-cache");
            }

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (Exception e) {
            log.error("파일 미리보기 중 오류: {}", e.getMessage(), e);
            throw new FileNotFoundException("파일을 미리볼 수 없습니다.");
        }
    }

    /**
     * 공개 파일 직접 접근 (인증 불필요)
     */
    @GetMapping("/public/{fileId}")
    @Operation(summary = "공개 파일 접근", description = "공개 파일에 인증 없이 접근합니다.")
    public ResponseEntity<Resource> getPublicFile(
            @Parameter(description = "파일 ID") @PathVariable Long fileId,
            @Parameter(description = "첨부파일로 다운로드 여부") @RequestParam(defaultValue = "false") boolean download
    ) {
        log.debug("공개 파일 접근 요청: {}", fileId);

        File file = fileRepository.findActiveFileById(fileId)
                .orElseThrow(() -> new FileNotFoundException("파일을 찾을 수 없습니다."));

        // 공개 파일이 아닌 경우
        if (!file.getIsPublic()) {
            throw new WorkspaceAccessDeniedException("비공개 파일입니다.");
        }

        try {
            Resource resource = fileStorageService.loadFileAsResource(file.getFilePath());

            String contentType = determineContentType(file);
            String encodedFileName = URLEncoder.encode(file.getOriginalName(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setCacheControl("public, max-age=86400"); // 24시간 캐시

            if (download) {
                headers.setContentDispositionFormData("attachment", encodedFileName);
            } else {
                headers.setContentDispositionFormData("inline", encodedFileName);
            }

            // 다운로드 수 증가
            fileRepository.incrementDownloadCount(fileId);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (Exception e) {
            log.error("공개 파일 접근 중 오류: {}", e.getMessage(), e);
            throw new FileNotFoundException("파일을 로드할 수 없습니다.");
        }
    }

    // ===== 내부 헬퍼 메서드 =====

    /**
     * 파일 접근 권한 확인
     */
    private File getAccessibleFile(Long fileId, User user) {
        File file = fileRepository.findActiveFileById(fileId)
                .orElseThrow(() -> new FileNotFoundException("파일을 찾을 수 없습니다."));

        // 공개 파일이면 누구나 접근 가능
        if (file.getIsPublic()) {
            return file;
        }

        // 비공개 파일은 인증된 사용자만
        if (user == null) {
            throw new WorkspaceAccessDeniedException("파일에 접근할 권한이 없습니다.");
        }

        // 워크스페이스 멤버 또는 소유자 확인
        if (!file.getWorkspace().isOwner(user) && !file.getWorkspace().isMember(user)) {
            throw new WorkspaceAccessDeniedException("파일에 접근할 권한이 없습니다.");
        }

        return file;
    }

    /**
     * Content-Type 결정
     */
    private String determineContentType(File file) {
        String mimeType = file.getMimeType();

        // MIME 타입이 있으면 사용
        if (mimeType != null && !mimeType.isEmpty()) {
            return mimeType;
        }

        // 확장자 기반으로 결정
        String extension = file.getFileExtension().toLowerCase();

        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "svg" -> "image/svg+xml";
            case "pdf" -> "application/pdf";
            case "txt" -> "text/plain";
            case "html" -> "text/html";
            case "css" -> "text/css";
            case "js" -> "application/javascript";
            case "json" -> "application/json";
            case "xml" -> "application/xml";
            case "mp4" -> "video/mp4";
            case "mp3" -> "audio/mpeg";
            case "zip" -> "application/zip";
            default -> "application/octet-stream";
        };
    }
}