package com.stacknote.back.domain.file.entity;

import com.stacknote.back.domain.page.entity.Page;
import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.domain.workspace.entity.Workspace;
import com.stacknote.back.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 파일 엔티티
 * 업로드된 파일들의 메타데이터와 저장 정보를 관리
 */
@Entity
@Table(name = "files", indexes = {
        @Index(name = "idx_file_workspace", columnList = "workspace_id"),
        @Index(name = "idx_file_page", columnList = "page_id"),
        @Index(name = "idx_file_uploaded_by", columnList = "uploaded_by"),
        @Index(name = "idx_file_file_type", columnList = "file_type"),
        @Index(name = "idx_file_created_at", columnList = "created_at")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class File extends BaseEntity {

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName; // 원본 파일명

    @Column(name = "stored_name", nullable = false, length = 255)
    private String storedName; // 저장된 파일명 (UUID 등)

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath; // 파일 저장 경로

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl; // 파일 접근 URL

    @Column(name = "file_size", nullable = false)
    private Long fileSize; // 파일 크기 (바이트)

    @Column(name = "mime_type", length = 100)
    private String mimeType; // MIME 타입

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private FileType fileType; // 파일 분류

    @Column(name = "checksum", length = 64)
    private String checksum; // 파일 무결성 검증용 해시

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id")
    private Page page; // 연결된 페이지 (null이면 워크스페이스 전체 파일)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Column(name = "download_count", nullable = false)
    @Builder.Default
    private Long downloadCount = 0L;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = false; // 공개 파일 여부

    @Column(name = "description", length = 500)
    private String description; // 파일 설명

    // 이미지 파일 전용 메타데이터
    @Column(name = "image_width")
    private Integer imageWidth;

    @Column(name = "image_height")
    private Integer imageHeight;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl; // 썸네일 URL

    // ===== 비즈니스 로직 메서드 =====

    /**
     * 파일 정보 업데이트
     */
    public void updateInfo(String description, Boolean isPublic) {
        if (description != null) {
            this.description = description;
        }
        if (isPublic != null) {
            this.isPublic = isPublic;
        }
    }

    /**
     * 페이지 연결
     */
    public void attachToPage(Page page) {
        if (page != null && !page.getWorkspace().getId().equals(this.workspace.getId())) {
            throw new IllegalArgumentException("파일과 페이지는 같은 워크스페이스에 속해야 합니다.");
        }
        this.page = page;
    }

    /**
     * 페이지 연결 해제
     */
    public void detachFromPage() {
        this.page = null;
    }

    /**
     * 다운로드 수 증가
     */
    public void incrementDownloadCount() {
        this.downloadCount++;
    }

    /**
     * 이미지 메타데이터 설정
     */
    public void setImageMetadata(Integer width, Integer height, String thumbnailUrl) {
        if (this.fileType == FileType.IMAGE) {
            this.imageWidth = width;
            this.imageHeight = height;
            this.thumbnailUrl = thumbnailUrl;
        }
    }

    /**
     * 파일 크기를 사람이 읽기 쉬운 형태로 반환
     */
    public String getFormattedFileSize() {
        if (fileSize == null) return "0 B";

        long bytes = fileSize;
        if (bytes < 1024) return bytes + " B";

        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * 이미지 파일 여부 확인
     */
    public boolean isImage() {
        return this.fileType == FileType.IMAGE;
    }

    /**
     * 문서 파일 여부 확인
     */
    public boolean isDocument() {
        return this.fileType == FileType.DOCUMENT;
    }

    /**
     * 비디오 파일 여부 확인
     */
    public boolean isVideo() {
        return this.fileType == FileType.VIDEO;
    }

    /**
     * 오디오 파일 여부 확인
     */
    public boolean isAudio() {
        return this.fileType == FileType.AUDIO;
    }

    /**
     * 파일 확장자 추출
     */
    public String getFileExtension() {
        if (originalName == null || !originalName.contains(".")) {
            return "";
        }
        return originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 썸네일이 있는지 확인
     */
    public boolean hasThumbnail() {
        return thumbnailUrl != null && !thumbnailUrl.trim().isEmpty();
    }

    /**
     * 웹에서 미리보기 가능한 파일인지 확인
     */
    public boolean isPreviewable() {
        return isImage() ||
                (isDocument() && (mimeType != null && mimeType.equals("application/pdf"))) ||
                (mimeType != null && mimeType.startsWith("text/"));
    }

    /**
     * 파일 타입
     */
    public enum FileType {
        IMAGE,      // 이미지 파일 (jpg, png, gif, svg 등)
        DOCUMENT,   // 문서 파일 (pdf, doc, txt 등)
        VIDEO,      // 비디오 파일 (mp4, avi, mov 등)
        AUDIO,      // 오디오 파일 (mp3, wav, flac 등)
        ARCHIVE,    // 압축 파일 (zip, rar, tar 등)
        CODE,       // 코드 파일 (js, java, py 등)
        OTHER       // 기타
    }
}