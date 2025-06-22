package com.stacknote.back.domain.comment.controller.query;

import com.stacknote.back.domain.comment.dto.response.CommentResponse;
import com.stacknote.back.domain.comment.dto.response.CommentSummaryResponse;
import com.stacknote.back.domain.comment.service.query.CommentQueryService;
import com.stacknote.back.domain.page.entity.Page;
import com.stacknote.back.domain.page.repository.PageRepository;
import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.domain.user.repository.UserRepository;
import com.stacknote.back.global.dto.ApiResponse;
import com.stacknote.back.global.exception.custom.EntityNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 댓글 조회 컨트롤러
 */
@Tag(name = "댓글 조회", description = "댓글 조회 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentQueryController {

    private final CommentQueryService commentQueryService;
    private final PageRepository pageRepository;
    private final UserRepository userRepository;

    @Operation(summary = "댓글 상세 조회", description = "댓글 ID로 댓글 상세 정보를 조회합니다.")
    @GetMapping("/{commentId}")
    public ApiResponse<CommentResponse> getComment(
            @Parameter(description = "댓글 ID") @PathVariable Long commentId,
            Authentication authentication) {

        Long currentUserId = getCurrentUserId(authentication);
        CommentResponse comment = commentQueryService.getCommentById(commentId, currentUserId);

        return ApiResponse.success("댓글 조회 성공", comment);
    }

    @Operation(summary = "페이지의 댓글 목록 조회", description = "특정 페이지의 최상위 댓글 목록을 조회합니다.")
    @GetMapping("/page/{pageId}")
    public ApiResponse<List<CommentResponse>> getCommentsByPage(
            @Parameter(description = "페이지 ID") @PathVariable Long pageId,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        Page targetPage = pageRepository.findById(pageId)
                .orElseThrow(() -> new EntityNotFoundException("페이지를 찾을 수 없습니다."));

        Long currentUserId = getCurrentUserId(authentication);
        List<CommentResponse> comments;

        if (page == 0 && size == 20) {
            // 기본값인 경우 페이징 없이 모든 댓글 조회
            comments = commentQueryService.getRootCommentsByPage(targetPage, currentUserId);
        } else {
            comments = commentQueryService.getRootCommentsByPage(targetPage, page, size, currentUserId);
        }

        return ApiResponse.success("페이지 댓글 목록 조회 성공", comments);
    }

    @Operation(summary = "대댓글 목록 조회", description = "특정 댓글의 대댓글 목록을 조회합니다.")
    @GetMapping("/{commentId}/replies")
    public ApiResponse<List<CommentResponse>> getReplies(
            @Parameter(description = "부모 댓글 ID") @PathVariable Long commentId,
            Authentication authentication) {

        Long currentUserId = getCurrentUserId(authentication);
        List<CommentResponse> replies = commentQueryService.getRepliesByParent(commentId, currentUserId);

        return ApiResponse.success("대댓글 목록 조회 성공", replies);
    }

    @Operation(summary = "페이지의 댓글 수 조회", description = "특정 페이지의 전체 댓글 수를 조회합니다.")
    @GetMapping("/page/{pageId}/count")
    public ApiResponse<Long> getCommentCountByPage(
            @Parameter(description = "페이지 ID") @PathVariable Long pageId) {

        Page targetPage = pageRepository.findById(pageId)
                .orElseThrow(() -> new EntityNotFoundException("페이지를 찾을 수 없습니다."));

        long count = commentQueryService.getCommentCountByPage(targetPage);

        return ApiResponse.success("페이지 댓글 수 조회 성공", count);
    }

    @Operation(summary = "사용자 댓글 목록 조회", description = "특정 사용자가 작성한 댓글 목록을 조회합니다.")
    @GetMapping("/user/{userId}")
    public ApiResponse<List<CommentSummaryResponse>> getCommentsByUser(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        List<CommentSummaryResponse> comments = commentQueryService.getCommentsByAuthor(author, page, size);

        return ApiResponse.success("사용자 댓글 목록 조회 성공", comments);
    }

    @Operation(summary = "기간별 댓글 조회", description = "특정 기간 내에 작성된 댓글을 조회합니다.")
    @GetMapping("/page/{pageId}/date-range")
    public ApiResponse<List<CommentSummaryResponse>> getCommentsByDateRange(
            @Parameter(description = "페이지 ID") @PathVariable Long pageId,
            @Parameter(description = "시작 날짜") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "종료 날짜") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Page targetPage = pageRepository.findById(pageId)
                .orElseThrow(() -> new EntityNotFoundException("페이지를 찾을 수 없습니다."));

        List<CommentSummaryResponse> comments = commentQueryService.getCommentsByDateRange(targetPage, startDate, endDate);

        return ApiResponse.success("기간별 댓글 조회 성공", comments);
    }

    @Operation(summary = "최근 댓글 조회", description = "최근에 작성된 댓글 목록을 조회합니다.")
    @GetMapping("/recent")
    public ApiResponse<List<CommentSummaryResponse>> getRecentComments(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {

        List<CommentSummaryResponse> comments = commentQueryService.getRecentComments(page, size);

        return ApiResponse.success("최근 댓글 조회 성공", comments);
    }

    @Operation(summary = "워크스페이스 최근 댓글 조회", description = "특정 워크스페이스의 최근 댓글을 조회합니다.")
    @GetMapping("/workspace/{workspaceId}/recent")
    public ApiResponse<List<CommentSummaryResponse>> getRecentCommentsByWorkspace(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {

        List<CommentSummaryResponse> comments = commentQueryService.getRecentCommentsByWorkspace(workspaceId, page, size);

        return ApiResponse.success("워크스페이스 최근 댓글 조회 성공", comments);
    }

    @Operation(summary = "인기 댓글 조회", description = "좋아요 수가 많은 인기 댓글을 조회합니다.")
    @GetMapping("/page/{pageId}/popular")
    public ApiResponse<List<CommentResponse>> getPopularComments(
            @Parameter(description = "페이지 ID") @PathVariable Long pageId,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        Page targetPage = pageRepository.findById(pageId)
                .orElseThrow(() -> new EntityNotFoundException("페이지를 찾을 수 없습니다."));

        Long currentUserId = getCurrentUserId(authentication);
        List<CommentResponse> comments = commentQueryService.getPopularCommentsByPage(targetPage, page, size, currentUserId);

        return ApiResponse.success("인기 댓글 조회 성공", comments);
    }

    @Operation(summary = "멘션 댓글 조회", description = "멘션이 포함된 댓글을 조회합니다.")
    @GetMapping("/mentions")
    public ApiResponse<List<CommentSummaryResponse>> getCommentsWithMentions(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {

        List<CommentSummaryResponse> comments = commentQueryService.getCommentsWithMentions(page, size);

        return ApiResponse.success("멘션 댓글 조회 성공", comments);
    }

    @Operation(summary = "사용자 멘션 댓글 조회", description = "특정 사용자를 멘션한 댓글을 조회합니다.")
    @GetMapping("/mentions/{username}")
    public ApiResponse<List<CommentSummaryResponse>> getCommentsByMentionedUser(
            @Parameter(description = "사용자명") @PathVariable String username,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {

        List<CommentSummaryResponse> comments = commentQueryService.getCommentsByMentionedUser(username, page, size);

        return ApiResponse.success("사용자 멘션 댓글 조회 성공", comments);
    }

    @Operation(summary = "댓글 검색", description = "댓글 내용으로 검색합니다.")
    @GetMapping("/page/{pageId}/search")
    public ApiResponse<List<CommentResponse>> searchComments(
            @Parameter(description = "페이지 ID") @PathVariable Long pageId,
            @Parameter(description = "검색 키워드") @RequestParam String keyword,
            Authentication authentication) {

        Page targetPage = pageRepository.findById(pageId)
                .orElseThrow(() -> new EntityNotFoundException("페이지를 찾을 수 없습니다."));

        Long currentUserId = getCurrentUserId(authentication);
        List<CommentResponse> comments = commentQueryService.searchCommentsByContent(targetPage, keyword, currentUserId);

        return ApiResponse.success("댓글 검색 성공", comments);
    }

    /**
     * 현재 사용자 ID 조회
     */
    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}