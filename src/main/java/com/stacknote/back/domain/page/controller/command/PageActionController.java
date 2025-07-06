package com.stacknote.back.domain.page.controller.command;

import com.stacknote.back.domain.page.service.command.PageActionService;
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

/**
 * 페이지 액션 컨트롤러
 * 즐겨찾기, 방문 기록 등 페이지 관련 부가 기능 제공
 */
@Slf4j
@RestController
@RequestMapping("/pages")
@RequiredArgsConstructor
@Tag(name = "Page Actions", description = "페이지 액션 API")
public class PageActionController {

    private final PageActionService pageActionService;

    /**
     * 페이지 즐겨찾기 토글
     */
    @PostMapping("/{pageId}/favorite")
    @Operation(summary = "즐겨찾기 토글", description = "페이지를 즐겨찾기에 추가/제거합니다.")
    public ResponseEntity<ApiResponse<Boolean>> toggleFavorite(
            @Parameter(description = "페이지 ID") @PathVariable Long pageId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("페이지 즐겨찾기 토글 요청: 페이지 {}, 사용자 {}", pageId, currentUser.getId());

        boolean isFavorited = pageActionService.toggleFavorite(pageId, currentUser);
        String message = isFavorited ? "즐겨찾기에 추가되었습니다." : "즐겨찾기에서 제거되었습니다.";

        return ResponseEntity.ok(ApiResponse.success(message, isFavorited));
    }

    /**
     * 페이지 방문 기록
     * - 최근 방문 페이지 추적을 위한 API
     */
    @PostMapping("/{pageId}/visit")
    @Operation(summary = "페이지 방문 기록", description = "페이지 방문을 기록합니다.")
    public ResponseEntity<ApiResponse<Void>> recordPageVisit(
            @Parameter(description = "페이지 ID") @PathVariable Long pageId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.debug("페이지 방문 기록: 페이지 {}, 사용자 {}", pageId, currentUser.getId());

        pageActionService.recordPageVisit(pageId, currentUser);

        return ResponseEntity.ok(ApiResponse.success("페이지 방문이 기록되었습니다."));
    }
}