package com.stacknote.back.domain.tag.controller.command;

import com.stacknote.back.domain.tag.dto.request.TagAddToPageRequest;
import com.stacknote.back.domain.tag.dto.request.TagCreateRequest;
import com.stacknote.back.domain.tag.dto.request.TagUpdateRequest;
import com.stacknote.back.domain.tag.dto.response.TagResponse;
import com.stacknote.back.domain.tag.service.command.TagCommandService;
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

import java.util.List;

/**
 * 태그 명령 컨트롤러
 */
@Tag(name = "태그 관리", description = "태그 생성, 수정, 삭제 관련 API")
@Slf4j
@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagCommandController {

    private final TagCommandService tagCommandService;

    @Operation(summary = "태그 생성", description = "새로운 태그를 생성합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TagResponse> createTag(
            @Valid @RequestBody TagCreateRequest request) {

        TagResponse tag = tagCommandService.createTag(request);

        return ApiResponse.success("태그 생성 성공", tag);
    }

    @Operation(summary = "태그 수정", description = "태그 정보를 수정합니다.")
    @PutMapping("/{tagId}")
    public ApiResponse<TagResponse> updateTag(
            @Parameter(description = "태그 ID") @PathVariable Long tagId,
            @Valid @RequestBody TagUpdateRequest request) {

        TagResponse tag = tagCommandService.updateTag(tagId, request);

        return ApiResponse.success("태그 수정 성공", tag);
    }

    @Operation(summary = "태그 삭제", description = "태그를 삭제합니다.")
    @DeleteMapping("/{tagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteTag(
            @Parameter(description = "태그 ID") @PathVariable Long tagId) {

        tagCommandService.deleteTag(tagId);

        return ApiResponse.success("태그 삭제 성공");
    }

    @Operation(summary = "페이지에 태그 추가", description = "페이지에 하나 이상의 태그를 추가합니다.")
    @PostMapping("/page")
    public ApiResponse<List<TagResponse>> addTagsToPage(
            @Valid @RequestBody TagAddToPageRequest request,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        List<TagResponse> addedTags = tagCommandService.addTagsToPage(request, currentUser);

        return ApiResponse.success("페이지 태그 추가 성공", addedTags);
    }

    @Operation(summary = "페이지에서 태그 제거", description = "페이지에서 특정 태그를 제거합니다.")
    @DeleteMapping("/page/{pageId}/tag/{tagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> removeTagFromPage(
            @Parameter(description = "페이지 ID") @PathVariable Long pageId,
            @Parameter(description = "태그 ID") @PathVariable Long tagId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        tagCommandService.removeTagFromPage(pageId, tagId, currentUser);

        return ApiResponse.success("페이지 태그 제거 성공");
    }

    @Operation(summary = "페이지의 모든 태그 제거", description = "페이지의 모든 태그를 제거합니다.")
    @DeleteMapping("/page/{pageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> removeAllTagsFromPage(
            @Parameter(description = "페이지 ID") @PathVariable Long pageId) {

        tagCommandService.removeAllTagsFromPage(pageId);

        return ApiResponse.success("페이지 모든 태그 제거 성공");
    }

    /**
     * 현재 사용자 조회
     */
    private User getCurrentUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }
}