package com.stacknote.back.domain.file.controller.query;

import com.stacknote.back.domain.file.dto.response.FileResponse;
import com.stacknote.back.domain.file.service.query.FileQueryService;
import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 전역 파일 쿼리 컨트롤러 (워크스페이스 독립적)
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "Global File Queries", description = "전역 파일 조회 API")
public class GlobalFileQueryController {

    private final FileQueryService fileQueryService;

    /**
     * 파일 상세 조회
     */
    @GetMapping("/{fileId}")
    @Operation(summary = "파일 상세 조회", description = "파일 ID로 파일의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<FileResponse>> getFile(
            @Parameter(description = "파일 ID") @PathVariable Long fileId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("파일 상세 조회: {}, 사용자: {}", fileId, currentUser.getId());

        FileResponse file = fileQueryService.getFile(fileId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("파일 조회 완료", file));
    }

    /**
     * 내가 업로드한 파일들 조회
     */
    @GetMapping("/my-uploads")
    @Operation(summary = "내가 업로드한 파일", description = "현재 사용자가 업로드한 파일 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<FileResponse>>> getMyUploadedFiles(
            @Parameter(description = "최대 개수") @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("내가 업로드한 파일 조회: 사용자: {}", currentUser.getId());

        List<FileResponse> files = fileQueryService.getUserUploadedFiles(currentUser, limit);

        return ResponseEntity.ok(ApiResponse.success("내가 업로드한 파일 목록 조회 완료", files));
    }
}