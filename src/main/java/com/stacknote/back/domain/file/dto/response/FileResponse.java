package com.stacknote.back.domain.file.dto.response;

import com.stacknote.back.domain.file.entity.File;
import com.stacknote.back.domain.user.dto.response.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 파일 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {

    private Long id;
    private String originalName;
    private String storedName;
    private String fileUrl;
    private Long fileSize;
    private String formattedFileSize;
    private String mimeType;
    private String fileType;
    private String checksum;
    private Long workspaceId;
    private String workspaceName;
    private Long pageId;
    private String pageTitle;
    private UserResponse uploadedBy;
    private Long downloadCount;
    private Boolean isPublic;
    private String description;

    // 이미지 전용 필드
    private Integer imageWidth;
    private Integer imageHeight;
    private String thumbnailUrl;
    private Boolean hasThumbnail;
    private Boolean isPreviewable;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * File 엔티티로부터 FileResponse 생성
     */
    public static FileResponse from(File file) {
        return FileResponse.builder()
                .id(file.getId())
                .originalName(file.getOriginalName())
                .storedName(file.getStoredName())
                .fileUrl(file.getFileUrl())
                .fileSize(file.getFileSize())
                .formattedFileSize(file.getFormattedFileSize())
                .mimeType(file.getMimeType())
                .fileType(file.getFileType().name())
                .checksum(file.getChecksum())
                .workspaceId(file.getWorkspace().getId())
                .workspaceName(file.getWorkspace().getName())
                .pageId(file.getPage() != null ? file.getPage().getId() : null)
                .pageTitle(file.getPage() != null ? file.getPage().getTitle() : null)
                .uploadedBy(UserResponse.from(file.getUploadedBy()))
                .downloadCount(file.getDownloadCount())
                .isPublic(file.getIsPublic())
                .description(file.getDescription())
                .imageWidth(file.getImageWidth())
                .imageHeight(file.getImageHeight())
                .thumbnailUrl(file.getThumbnailUrl())
                .hasThumbnail(file.hasThumbnail())
                .isPreviewable(file.isPreviewable())
                .createdAt(file.getCreatedAt())
                .updatedAt(file.getUpdatedAt())
                .build();
    }

    /**
     * 파일 메타데이터만 포함한 간단한 응답 생성 (목록용)
     */
    public static FileResponse fromSummary(File file) {
        return FileResponse.builder()
                .id(file.getId())
                .originalName(file.getOriginalName())
                .fileUrl(file.getFileUrl())
                .fileSize(file.getFileSize())
                .formattedFileSize(file.getFormattedFileSize())
                .mimeType(file.getMimeType())
                .fileType(file.getFileType().name())
                .uploadedBy(UserResponse.from(file.getUploadedBy()))
                .downloadCount(file.getDownloadCount())
                .isPublic(file.getIsPublic())
                .thumbnailUrl(file.getThumbnailUrl())
                .hasThumbnail(file.hasThumbnail())
                .isPreviewable(file.isPreviewable())
                .createdAt(file.getCreatedAt())
                .build();
    }
}