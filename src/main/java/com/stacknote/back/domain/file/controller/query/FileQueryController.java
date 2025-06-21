package com.stacknote.back.domain.file.controller.query;

import com.stacknote.back.domain.file.dto.response.FileResponse;
import com.stacknote.back.domain.file.entity.File;
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
 * 파일 쿼리 컨트롤러
 * 파일 조회, 검색 등의 읽기 전용 기능 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/workspaces/{workspaceId}/files")
@RequiredArgsConstructor
@Tag(name = "File Queries", description = "파일 조회 관리 API")
public class FileQueryController {

    private final FileQueryService fileQueryService;

    /**
     * 워크스페이스의 모든 파일 목록 조회
     */
    @GetMapping
    @Operation(summary = "워크스페이스 파일 목록", description = "워크스페이스의 모든 파일 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<FileResponse>>> getWorkspaceFiles(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "-1") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("워크스페이스 파일 목록 조회: {}, 사용자: {}", workspaceId, currentUser.getId());

        List<FileResponse> files = (page >= 0) ?
                fileQueryService.getWorkspaceFiles(workspaceId, currentUser, page, size) :
                fileQueryService.getWorkspaceFiles(workspaceId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("파일 목록 조회 완료", files));
    }

    /**
     * 특정 페이지의 파일들 조회
     */
    @GetMapping("/pages/{pageId}")
    @Operation(summary = "페이지 파일 목록", description = "특정 페이지에 연결된 파일들을 조회합니다.")
    public ResponseEntity<ApiResponse<List<FileResponse>>> getPageFiles(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "페이지 ID") @PathVariable Long pageId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("페이지 파일 목록 조회: {}, 사용자: {}", pageId, currentUser.getId());

        List<FileResponse> files = fileQueryService.getPageFiles(pageId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("페이지 파일 목록 조회 완료", files));
    }

    /**
     * 연결되지 않은 파일들 조회
     */
    @GetMapping("/unattached")
    @Operation(summary = "연결되지 않은 파일 목록", description = "워크스페이스에서 페이지에 연결되지 않은 파일들을 조회합니다.")
    public ResponseEntity<ApiResponse<List<FileResponse>>> getUnattachedFiles(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("연결되지 않은 파일 목록 조회: {}, 사용자: {}", workspaceId, currentUser.getId());

        List<FileResponse> files = fileQueryService.getUnattachedFiles(workspaceId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("연결되지 않은 파일 목록 조회 완료", files));
    }

    /**
     * 파일 타입별 조회
     */
    @GetMapping("/type/{fileType}")
    @Operation(summary = "파일 타입별 조회", description = "특정 타입의 파일들을 조회합니다.")
    public ResponseEntity<ApiResponse<List<FileResponse>>> getFilesByType(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "파일 타입") @PathVariable File.FileType fileType,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("파일 타입별 조회: {}, 타입: {}, 사용자: {}", workspaceId, fileType, currentUser.getId());

        List<FileResponse> files = fileQueryService.getFilesByType(workspaceId, fileType, currentUser);

        return ResponseEntity.ok(ApiResponse.success("파일 타입별 조회 완료", files));
    }

    /**
     * 이미지 파일들만 조회
     */
    @GetMapping("/images")
    @Operation(summary = "이미지 파일 목록", description = "워크스페이스의 모든 이미지 파일을 조회합니다.")
    public ResponseEntity<ApiResponse<List<FileResponse>>> getImageFiles(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("이미지 파일 목록 조회: {}, 사용자: {}", workspaceId, currentUser.getId());

        List<FileResponse> files = fileQueryService.getImageFiles(workspaceId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("이미지 파일 목록 조회 완료", files));
    }

    /**
     * 파일 검색
     */
    @GetMapping("/search")
    @Operation(summary = "파일 검색", description = "파일명으로 파일을 검색합니다.")
    public ResponseEntity<ApiResponse<List<FileResponse>>> searchFiles(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "검색 키워드") @RequestParam String keyword,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("파일 검색: 워크스페이스: {}, 키워드: {}, 사용자: {}", workspaceId, keyword, currentUser.getId());

        List<FileResponse> files = fileQueryService.searchFiles(workspaceId, keyword, currentUser);

        return ResponseEntity.ok(ApiResponse.success("파일 검색 완료", files));
    }

    /**
     * 최근 업로드된 파일들 조회
     */
    @GetMapping("/recent")
    @Operation(summary = "최근 업로드된 파일", description = "최근 업로드된 파일들을 조회합니다.")
    public ResponseEntity<ApiResponse<List<FileResponse>>> getRecentFiles(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "조회할 일수") @RequestParam(defaultValue = "7") int days,
            @Parameter(description = "최대 개수") @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("최근 업로드된 파일 조회: 워크스페이스: {}, 사용자: {}", workspaceId, currentUser.getId());

        List<FileResponse> files = fileQueryService.getRecentFiles(workspaceId, currentUser, days, limit);

        return ResponseEntity.ok(ApiResponse.success("최근 업로드된 파일 조회 완료", files));
    }

    /**
     * 인기 파일들 조회
     */
    @GetMapping("/popular")
    @Operation(summary = "인기 파일 목록", description = "다운로드 수가 높은 인기 파일들을 조회합니다.")
    public ResponseEntity<ApiResponse<List<FileResponse>>> getPopularFiles(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "최대 개수") @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("인기 파일 목록 조회: 워크스페이스: {}, 사용자: {}", workspaceId, currentUser.getId());

        List<FileResponse> files = fileQueryService.getPopularFiles(workspaceId, currentUser, limit);

        return ResponseEntity.ok(ApiResponse.success("인기 파일 목록 조회 완료", files));
    }

    /**
     * 워크스페이스 파일 통계 조회
     */
    @GetMapping("/statistics")
    @Operation(summary = "파일 통계", description = "워크스페이스의 파일 통계를 조회합니다.")
    public ResponseEntity<ApiResponse<FileQueryService.FileStatisticsResponse>> getFileStatistics(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("파일 통계 조회: 워크스페이스: {}, 사용자: {}", workspaceId, currentUser.getId());

        FileQueryService.FileStatisticsResponse statistics =
                fileQueryService.getWorkspaceFileStatistics(workspaceId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("파일 통계 조회 완료", statistics));
    }
}