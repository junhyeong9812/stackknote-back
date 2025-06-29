package com.stacknote.back.domain.page.controller.command;

import com.stacknote.back.domain.page.dto.request.PageCreateRequest;
import com.stacknote.back.domain.page.dto.request.PageDuplicateRequest;
import com.stacknote.back.domain.page.dto.request.PageMoveRequest;
import com.stacknote.back.domain.page.dto.request.PageUpdateRequest;
import com.stacknote.back.domain.page.dto.response.PageResponse;
import com.stacknote.back.domain.page.service.command.PageCommandService;
import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 페이지 명령 컨트롤러
 * 페이지 생성, 수정, 삭제, 이동 등의 명령 기능 제공
 */
@Slf4j
@RestController
@RequestMapping("/workspaces/{workspaceId}/pages")
@RequiredArgsConstructor
@Tag(name = "Page Commands", description = "페이지 명령 관리 API")
public class PageCommandController {

    private final PageCommandService pageCommandService;

    /**
     * 페이지 생성
     */
    @PostMapping
    @Operation(summary = "페이지 생성", description = "워크스페이스에 새로운 페이지를 생성합니다.")
    public ResponseEntity<ApiResponse<PageResponse>> createPage(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody PageCreateRequest request
    ) {
        log.info("페이지 생성 요청: {}, 워크스페이스: {}, 사용자: {}", request.getTitle(), workspaceId, currentUser.getId());

        PageResponse response = pageCommandService.createPage(workspaceId, currentUser, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("페이지가 생성되었습니다.", response));
    }

    /**
     * 페이지 수정
     */
    @PutMapping("/{pageId}")
    @Operation(summary = "페이지 수정", description = "페이지 정보를 수정합니다.")
    public ResponseEntity<ApiResponse<PageResponse>> updatePage(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "페이지 ID") @PathVariable Long pageId,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody PageUpdateRequest request
    ) {
        log.info("페이지 수정 요청: {}, 사용자: {}", pageId, currentUser.getId());

        PageResponse response = pageCommandService.updatePage(pageId, currentUser, request);

        return ResponseEntity.ok(ApiResponse.success("페이지가 수정되었습니다.", response));
    }

    /**
     * 페이지 삭제
     */
    @DeleteMapping("/{pageId}")
    @Operation(summary = "페이지 삭제", description = "페이지를 삭제합니다. 자식 페이지도 함께 삭제됩니다.")
    public ResponseEntity<ApiResponse<Void>> deletePage(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "페이지 ID") @PathVariable Long pageId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("페이지 삭제 요청: {}, 사용자: {}", pageId, currentUser.getId());

        pageCommandService.deletePage(pageId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("페이지가 삭제되었습니다."));
    }

    /**
     * 페이지 이동
     */
    @PutMapping("/{pageId}/move")
    @Operation(summary = "페이지 이동", description = "페이지를 다른 부모 페이지 아래로 이동합니다.")
    public ResponseEntity<ApiResponse<PageResponse>> movePage(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "페이지 ID") @PathVariable Long pageId,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody PageMoveRequest request
    ) {
        log.info("페이지 이동 요청: {}, 사용자: {}", pageId, currentUser.getId());

        PageResponse response = pageCommandService.movePage(pageId, currentUser, request);

        return ResponseEntity.ok(ApiResponse.success("페이지가 이동되었습니다.", response));
    }

    /**
     * 페이지 복제
     */
    @PostMapping("/{pageId}/duplicate")
    @Operation(summary = "페이지 복제", description = "페이지를 복제합니다. 옵션에 따라 자식 페이지도 함께 복제할 수 있습니다.")
    public ResponseEntity<ApiResponse<PageResponse>> duplicatePage(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "페이지 ID") @PathVariable Long pageId,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody PageDuplicateRequest request
    ) {
        log.info("페이지 복제 요청: {}, 사용자: {}", pageId, currentUser.getId());

        PageResponse response = pageCommandService.duplicatePage(pageId, currentUser, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("페이지가 복제되었습니다.", response));
    }

    /**
     * 페이지 공개/비공개 토글
     */
    @PostMapping("/{pageId}/toggle-visibility")
    @Operation(summary = "페이지 공개 상태 변경", description = "페이지의 공개/비공개 상태를 변경합니다.")
    public ResponseEntity<ApiResponse<PageResponse>> togglePageVisibility(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "페이지 ID") @PathVariable Long pageId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("페이지 공개 상태 변경 요청: {}, 사용자: {}", pageId, currentUser.getId());

        PageResponse response = pageCommandService.togglePageVisibility(pageId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("페이지 공개 상태가 변경되었습니다.", response));
    }

    /**
     * 페이지 잠금/잠금 해제 토글
     */
    @PostMapping("/{pageId}/toggle-lock")
    @Operation(summary = "페이지 잠금 상태 변경", description = "페이지의 잠금/잠금 해제 상태를 변경합니다.")
    public ResponseEntity<ApiResponse<PageResponse>> togglePageLock(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "페이지 ID") @PathVariable Long pageId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("페이지 잠금 상태 변경 요청: {}, 사용자: {}", pageId, currentUser.getId());

        PageResponse response = pageCommandService.togglePageLock(pageId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("페이지 잠금 상태가 변경되었습니다.", response));
    }

    /**
     * 페이지 버전 복원
     */
    @PostMapping("/{pageId}/restore/{version}")
    @Operation(summary = "페이지 버전 복원", description = "페이지를 특정 버전으로 복원합니다.")
    public ResponseEntity<ApiResponse<PageResponse>> restorePageVersion(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "페이지 ID") @PathVariable Long pageId,
            @Parameter(description = "복원할 버전 번호") @PathVariable Integer version,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("페이지 버전 복원 요청: {}, 버전: {}, 사용자: {}", pageId, version, currentUser.getId());

        PageResponse response = pageCommandService.restorePageVersion(pageId, version, currentUser);

        return ResponseEntity.ok(ApiResponse.success("페이지가 버전 " + version + "으로 복원되었습니다.", response));
    }
}