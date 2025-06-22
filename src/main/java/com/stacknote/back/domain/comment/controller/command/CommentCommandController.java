package com.stacknote.back.domain.comment.controller.command;

import com.stacknote.back.domain.comment.dto.request.CommentCreateRequest;
import com.stacknote.back.domain.comment.dto.request.CommentUpdateRequest;
import com.stacknote.back.domain.comment.dto.response.CommentResponse;
import com.stacknote.back.domain.comment.service.command.CommentCommandService;
import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 댓글 명령 컨트롤러
 */
@Tag(name = "댓글 관리", description = "댓글 생성, 수정, 삭제 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentCommandController {

    private final CommentCommandService commentCommandService;

    @Operation(summary = "댓글 생성", description = "새로운 댓글을 생성합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommentResponse> createComment(
            @Valid @RequestBody CommentCreateRequest request,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        CommentResponse comment = commentCommandService.createComment(request, currentUser);

        return ApiResponse.success("댓글 생성 성공", comment);
    }

    @Operation(summary = "댓글 수정", description = "댓글 내용을 수정합니다.")
    @PutMapping("/{commentId}")
    public ApiResponse<CommentResponse> updateComment(
            @Parameter(description = "댓글 ID") @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        CommentResponse comment = commentCommandService.updateComment(commentId, request, currentUser);

        return ApiResponse.success("댓글 수정 성공", comment);
    }

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteComment(
            @Parameter(description = "댓글 ID") @PathVariable Long commentId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        commentCommandService.deleteComment(commentId, currentUser);

        return ApiResponse.success("댓글 삭제 성공");
    }

    @Operation(summary = "댓글 좋아요", description = "댓글에 좋아요를 추가합니다.")
    @PostMapping("/{commentId}/like")
    public ApiResponse<Void> likeComment(
            @Parameter(description = "댓글 ID") @PathVariable Long commentId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        commentCommandService.incrementLikes(commentId, currentUser);

        return ApiResponse.success("댓글 좋아요 성공");
    }

    @Operation(summary = "댓글 좋아요 취소", description = "댓글 좋아요를 취소합니다.")
    @DeleteMapping("/{commentId}/like")
    public ApiResponse<Void> unlikeComment(
            @Parameter(description = "댓글 ID") @PathVariable Long commentId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        commentCommandService.decrementLikes(commentId, currentUser);

        return ApiResponse.success("댓글 좋아요 취소 성공");
    }

    /**
     * 현재 사용자 조회
     */
    private User getCurrentUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }
}