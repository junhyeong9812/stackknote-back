package com.stacknote.back.domain.tag.controller.query;

import com.stacknote.back.domain.tag.dto.response.TagResponse;
import com.stacknote.back.domain.tag.dto.response.TagStatisticsResponse;
import com.stacknote.back.domain.tag.dto.response.TagSummaryResponse;
import com.stacknote.back.domain.tag.service.query.TagQueryService;
import com.stacknote.back.domain.workspace.entity.Workspace;
import com.stacknote.back.domain.workspace.repository.WorkspaceRepository;
import com.stacknote.back.global.dto.ApiResponse;
import com.stacknote.back.global.exception.custom.EntityNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 태그 조회 컨트롤러
 */
@Tag(name = "태그 조회", description = "태그 조회 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagQueryController {

    private final TagQueryService tagQueryService;
    private final WorkspaceRepository workspaceRepository;

    @Operation(summary = "태그 상세 조회", description = "태그 ID로 태그 상세 정보를 조회합니다.")
    @GetMapping("/{tagId}")
    public ApiResponse<TagResponse> getTag(
            @Parameter(description = "태그 ID") @PathVariable Long tagId) {

        TagResponse tag = tagQueryService.getTagById(tagId);

        return ApiResponse.success("태그 조회 성공", tag);
    }

    @Operation(summary = "워크스페이스 태그 목록 조회", description = "워크스페이스의 모든 태그를 조회합니다.")
    @GetMapping("/workspace/{workspaceId}")
    public ApiResponse<List<TagResponse>> getTagsByWorkspace(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "정렬 방식 (usage: 사용횟수순, name: 이름순)") @RequestParam(defaultValue = "usage") String sort) {

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("워크스페이스를 찾을 수 없습니다."));

        List<TagResponse> tags;

        if ("name".equals(sort)) {
            tags = tagQueryService.getTagsByWorkspaceOrderByName(workspace);
        } else {
            tags = tagQueryService.getTagsByWorkspace(workspace);
        }

        return ApiResponse.success("워크스페이스 태그 목록 조회 성공", tags);
    }

    @Operation(summary = "태그 이름으로 조회", description = "워크스페이스 내에서 태그 이름으로 태그를 조회합니다.")
    @GetMapping("/workspace/{workspaceId}/name/{tagName}")
    public ApiResponse<TagResponse> getTagByName(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "태그 이름") @PathVariable String tagName) {

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("워크스페이스를 찾을 수 없습니다."));

        TagResponse tag = tagQueryService.getTagByWorkspaceAndName(workspace, tagName);

        return ApiResponse.success("태그 조회 성공", tag);
    }

    @Operation(summary = "인기 태그 조회", description = "워크스페이스의 인기 태그를 조회합니다.")
    @GetMapping("/workspace/{workspaceId}/popular")
    public ApiResponse<List<TagSummaryResponse>> getPopularTags(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "조회할 태그 수") @RequestParam(defaultValue = "10") int size) {

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("워크스페이스를 찾을 수 없습니다."));

        List<TagSummaryResponse> tags = tagQueryService.getPopularTagsByWorkspace(workspace, size);

        return ApiResponse.success("인기 태그 조회 성공", tags);
    }

    @Operation(summary = "사용되지 않는 태그 조회", description = "워크스페이스의 사용되지 않는 태그를 조회합니다.")
    @GetMapping("/workspace/{workspaceId}/unused")
    public ApiResponse<List<TagResponse>> getUnusedTags(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId) {

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("워크스페이스를 찾을 수 없습니다."));

        List<TagResponse> tags = tagQueryService.getUnusedTagsByWorkspace(workspace);

        return ApiResponse.success("사용되지 않는 태그 조회 성공", tags);
    }

    @Operation(summary = "시스템 태그 조회", description = "워크스페이스의 시스템 태그를 조회합니다.")
    @GetMapping("/workspace/{workspaceId}/system")
    public ApiResponse<List<TagResponse>> getSystemTags(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId) {

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("워크스페이스를 찾을 수 없습니다."));

        List<TagResponse> tags = tagQueryService.getSystemTagsByWorkspace(workspace);

        return ApiResponse.success("시스템 태그 조회 성공", tags);
    }

    @Operation(summary = "태그 검색", description = "태그 이름으로 검색합니다.")
    @GetMapping("/workspace/{workspaceId}/search")
    public ApiResponse<List<TagResponse>> searchTags(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "검색 키워드") @RequestParam String keyword) {

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("워크스페이스를 찾을 수 없습니다."));

        List<TagResponse> tags = tagQueryService.searchTagsByName(workspace, keyword);

        return ApiResponse.success("태그 검색 성공", tags);
    }

    @Operation(summary = "색상별 태그 조회", description = "특정 색상의 태그를 조회합니다.")
    @GetMapping("/workspace/{workspaceId}/color/{color}")
    public ApiResponse<List<TagResponse>> getTagsByColor(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "색상 코드 (#RRGGBB)") @PathVariable String color) {

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("워크스페이스를 찾을 수 없습니다."));

        List<TagResponse> tags = tagQueryService.getTagsByColor(workspace, "#" + color);

        return ApiResponse.success("색상별 태그 조회 성공", tags);
    }

    @Operation(summary = "페이지의 태그 조회", description = "특정 페이지에 연결된 태그를 조회합니다.")
    @GetMapping("/page/{pageId}")
    public ApiResponse<List<TagSummaryResponse>> getTagsByPage(
            @Parameter(description = "페이지 ID") @PathVariable Long pageId) {

        List<TagSummaryResponse> tags = tagQueryService.getTagsByPage(pageId);

        return ApiResponse.success("페이지 태그 조회 성공", tags);
    }

    @Operation(summary = "최근 생성된 태그 조회", description = "워크스페이스의 최근 생성된 태그를 조회합니다.")
    @GetMapping("/workspace/{workspaceId}/recent")
    public ApiResponse<List<TagResponse>> getRecentTags(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId,
            @Parameter(description = "조회할 태그 수") @RequestParam(defaultValue = "10") int size) {

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("워크스페이스를 찾을 수 없습니다."));

        List<TagResponse> tags = tagQueryService.getRecentTagsByWorkspace(workspace, size);

        return ApiResponse.success("최근 태그 조회 성공", tags);
    }

    @Operation(summary = "태그 통계 조회", description = "워크스페이스의 태그 통계를 조회합니다.")
    @GetMapping("/workspace/{workspaceId}/statistics")
    public ApiResponse<TagStatisticsResponse> getTagStatistics(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId) {

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("워크스페이스를 찾을 수 없습니다."));

        TagStatisticsResponse statistics = tagQueryService.getTagStatistics(workspace);

        return ApiResponse.success("태그 통계 조회 성공", statistics);
    }

    @Operation(summary = "태그 개수 조회", description = "워크스페이스의 태그 개수를 조회합니다.")
    @GetMapping("/workspace/{workspaceId}/count")
    public ApiResponse<Long> getTagCount(
            @Parameter(description = "워크스페이스 ID") @PathVariable Long workspaceId) {

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("워크스페이스를 찾을 수 없습니다."));

        long count = tagQueryService.getTagCountByWorkspace(workspace);

        return ApiResponse.success("태그 개수 조회 성공", count);
    }
}