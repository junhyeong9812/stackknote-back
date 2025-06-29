package com.stacknote.back.domain.file.controller.command;

import com.stacknote.back.domain.file.dto.request.FileUpdateRequest;
import com.stacknote.back.domain.file.dto.request.FileUploadRequest;
import com.stacknote.back.domain.file.dto.response.FileResponse;
import com.stacknote.back.domain.file.dto.response.FileUploadResponse;
import com.stacknote.back.domain.file.service.command.FileCommandService;
import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 명령 컨트롤러
 * 파일 업로드, 수정, 삭제 등의 명령 기능 제공
 */
@Slf4j
@RestController
@RequestMapping("/workspaces/{workspaceId}/files")
@RequiredArgsConstructor
@Tag(name = "File Commands", description = "파일 명령 관리 API")
public class FileCommandController {

    private final FileCommandService fileCommandService;

    /**
     * 파일 업로드
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "파일 업로드", description = "워크스페이스에 파일을 업로드합니다.")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "업로드할 파일") @RequestParam("file") MultipartFile file,
            @Parameter(description = "연결할 페이지 ID") @RequestParam(value = "pageId", required = false) Long pageId,
            @Parameter(description = "파일 설명") @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "공개 파일 여부") @RequestParam(value = "isPublic", defaultValue = "false") Boolean isPublic
    ) {
        log.info("파일 업로드 요청: {}, 워크스페이스: {}, 사용자: {}",
                file.getOriginalFilename(), workspaceId, currentUser.getId());

        FileUploadRequest request = new FileUploadRequest(pageId, description, isPublic);
        FileUploadResponse response = fileCommandService.uploadFile(workspaceId, currentUser, file, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("파일이 업로드되었습니다.", response));
    }

    /**
     * 파일 정보 수정
     */
    @PutMapping("/{fileId}")
    @Operation(summary = "파일 정보 수정", description = "파일의 메타데이터를 수정합니다.")
    public ResponseEntity<ApiResponse<FileResponse>> updateFile(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "파일 ID") @PathVariable Long fileId,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody FileUpdateRequest request
    ) {
        log.info("파일 정보 수정 요청: {}, 사용자: {}", fileId, currentUser.getId());

        FileResponse response = fileCommandService.updateFile(fileId, currentUser, request);

        return ResponseEntity.ok(ApiResponse.success("파일 정보가 수정되었습니다.", response));
    }

    /**
     * 파일 삭제
     */
    @DeleteMapping("/{fileId}")
    @Operation(summary = "파일 삭제", description = "파일을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "파일 ID") @PathVariable Long fileId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("파일 삭제 요청: {}, 사용자: {}", fileId, currentUser.getId());

        fileCommandService.deleteFile(fileId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("파일이 삭제되었습니다."));
    }

    /**
     * 파일 공개 상태 토글
     */
    @PostMapping("/{fileId}/toggle-visibility")
    @Operation(summary = "파일 공개 상태 변경", description = "파일의 공개/비공개 상태를 변경합니다.")
    public ResponseEntity<ApiResponse<FileResponse>> toggleFileVisibility(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "파일 ID") @PathVariable Long fileId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("파일 공개 상태 변경 요청: {}, 사용자: {}", fileId, currentUser.getId());

        FileResponse response = fileCommandService.toggleFileVisibility(fileId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("파일 공개 상태가 변경되었습니다.", response));
    }

    /**
     * 페이지에서 파일 연결 해제
     */
    @PostMapping("/{fileId}/detach")
    @Operation(summary = "파일 페이지 연결 해제", description = "파일과 페이지의 연결을 해제합니다.")
    public ResponseEntity<ApiResponse<FileResponse>> detachFileFromPage(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "파일 ID") @PathVariable Long fileId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("파일 페이지 연결 해제 요청: {}, 사용자: {}", fileId, currentUser.getId());

        FileResponse response = fileCommandService.detachFileFromPage(fileId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("파일 페이지 연결이 해제되었습니다.", response));
    }
}

