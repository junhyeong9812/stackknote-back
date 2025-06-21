package com.stacknote.back.domain.file.dto.response;

import com.stacknote.back.domain.file.entity.File;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 파일 업로드 응답 DTO
 * 업로드 완료 후 즉시 필요한 정보만 포함
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {

    private Long id;
    private String originalName;
    private String fileUrl;
    private String thumbnailUrl;
    private Long fileSize;
    private String formattedFileSize;
    private String mimeType;
    private String fileType;
    private Boolean isImage;
    private Boolean isPreviewable;
    private Integer imageWidth;
    private Integer imageHeight;

    /**
     * File 엔티티로부터 FileUploadResponse 생성
     */
    public static FileUploadResponse from(File file) {
        return FileUploadResponse.builder()
                .id(file.getId())
                .originalName(file.getOriginalName())
                .fileUrl(file.getFileUrl())
                .thumbnailUrl(file.getThumbnailUrl())
                .fileSize(file.getFileSize())
                .formattedFileSize(file.getFormattedFileSize())
                .mimeType(file.getMimeType())
                .fileType(file.getFileType().name())
                .isImage(file.isImage())
                .isPreviewable(file.isPreviewable())
                .imageWidth(file.getImageWidth())
                .imageHeight(file.getImageHeight())
                .build();
    }
}