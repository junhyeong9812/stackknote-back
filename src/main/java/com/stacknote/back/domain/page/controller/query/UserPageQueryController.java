package com.stacknote.back.domain.page.controller.query;

import com.stacknote.back.domain.page.dto.response.PageSummaryResponse;
import com.stacknote.back.domain.page.service.query.PageQueryService;
import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사용자 개별 페이지 쿼리 컨트롤러
 */
@RestController
@RequestMapping("/users/pages")
@RequiredArgsConstructor
@Tag(name = "User Page Queries", description = "사용자 페이지 조회 API")
public class UserPageQueryController {

    private final PageQueryService pageQueryService;

    /**
     * 내가 생성한 페이지 목록 조회
     */
    @GetMapping("/created")
    @Operation(summary = "내가 생성한 페이지", description = "현재 사용자가 생성한 페이지 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<PageSummaryResponse>>> getMyCreatedPages(
            @Parameter(description = "최대 개수") @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal User currentUser
    ) {
        List<PageSummaryResponse> pages = pageQueryService.getUserCreatedPages(currentUser, limit);
        return ResponseEntity.ok(ApiResponse.success("내가 생성한 페이지 목록 조회 완료", pages));
    }

    /**
     * 내가 최근 수정한 페이지 목록 조회
     */
    @GetMapping("/recent-modified")
    @Operation(summary = "내가 최근 수정한 페이지", description = "현재 사용자가 최근 수정한 페이지 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<PageSummaryResponse>>> getMyRecentlyModifiedPages(
            @Parameter(description = "최대 개수") @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal User currentUser
    ) {
        List<PageSummaryResponse> pages = pageQueryService.getUserRecentlyModifiedPages(currentUser, limit);
        return ResponseEntity.ok(ApiResponse.success("내가 최근 수정한 페이지 목록 조회 완료", pages));
    }
}