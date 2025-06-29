package com.stacknote.back.domain.file.controller.command;

import com.stacknote.back.domain.file.dto.request.FileUpdateRequest;
import com.stacknote.back.domain.file.dto.response.FileResponse;
import com.stacknote.back.domain.file.service.command.FileCommandService;
import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 전역 파일 명령 컨트롤러 (워크스페이스 독립적)
 * 파일 ID로 직접 파일을 관리하는 기능 제공
 */
@Slf4j
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(name = "Global File Commands", description = "전역 파일 명령 API")
public class GlobalFileCommandController {

    private final FileCommandService fileCommandService;

    /**
     * 파일 삭제 (전역)
     */
    @DeleteMapping("/{fileId}")
    @Operation(summary = "파일 삭제 (전역)", description = "파일 ID로 직접 파일을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @Parameter(description = "파일 ID") @PathVariable Long fileId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("파일 삭제 요청 (전역): {}, 사용자: {}", fileId, currentUser.getId());

        fileCommandService.deleteFile(fileId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("파일이 삭제되었습니다."));
    }

    /**
     * 파일 정보 수정 (전역)
     */
    @PutMapping("/{fileId}")
    @Operation(summary = "파일 정보 수정 (전역)", description = "파일 ID로 직접 파일 정보를 수정합니다.")
    public ResponseEntity<ApiResponse<FileResponse>> updateFile(
            @Parameter(description = "파일 ID") @PathVariable Long fileId,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody FileUpdateRequest request
    ) {
        log.info("파일 정보 수정 요청 (전역): {}, 사용자: {}", fileId, currentUser.getId());

        FileResponse response = fileCommandService.updateFile(fileId, currentUser, request);

        return ResponseEntity.ok(ApiResponse.success("파일 정보가 수정되었습니다.", response));
    }

    /**
     * 파일 공개 상태 토글 (전역)
     */
    @PostMapping("/{fileId}/toggle-visibility")
    @Operation(summary = "파일 공개 상태 변경 (전역)", description = "파일 ID로 직접 파일의 공개/비공개 상태를 변경합니다.")
    public ResponseEntity<ApiResponse<FileResponse>> toggleFileVisibility(
            @Parameter(description = "파일 ID") @PathVariable Long fileId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("파일 공개 상태 변경 요청 (전역): {}, 사용자: {}", fileId, currentUser.getId());

        FileResponse response = fileCommandService.toggleFileVisibility(fileId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("파일 공개 상태가 변경되었습니다.", response));
    }

    /**
     * 페이지에서 파일 연결 해제 (전역)
     */
    @PostMapping("/{fileId}/detach")
    @Operation(summary = "파일 페이지 연결 해제 (전역)", description = "파일 ID로 직접 파일과 페이지의 연결을 해제합니다.")
    public ResponseEntity<ApiResponse<FileResponse>> detachFileFromPage(
            @Parameter(description = "파일 ID") @PathVariable Long fileId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("파일 페이지 연결 해제 요청 (전역): {}, 사용자: {}", fileId, currentUser.getId());

        FileResponse response = fileCommandService.detachFileFromPage(fileId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("파일 페이지 연결이 해제되었습니다.", response));
    }
}