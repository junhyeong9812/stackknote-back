package com.stacknote.back.domain.tag.service.query;

import com.stacknote.back.domain.page.entity.Page;
import com.stacknote.back.domain.tag.dto.response.TagResponse;
import com.stacknote.back.domain.tag.dto.response.TagStatisticsResponse;
import com.stacknote.back.domain.tag.dto.response.TagSummaryResponse;
import com.stacknote.back.domain.tag.entity.Tag;
import com.stacknote.back.domain.tag.exception.TagNotFoundException;
import com.stacknote.back.domain.tag.repository.PageTagRepository;
import com.stacknote.back.domain.tag.repository.TagRepository;
import com.stacknote.back.domain.workspace.entity.Workspace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 태그 조회 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagQueryService {

    private final TagRepository tagRepository;
    private final PageTagRepository pageTagRepository;

    /**
     * 태그 ID로 태그 조회
     */
    public TagResponse getTagById(Long tagId) {
        Tag tag = tagRepository.findActiveTagById(tagId)
                .orElseThrow(() -> new TagNotFoundException(tagId));

        return TagResponse.from(tag);
    }

    /**
     * 워크스페이스의 모든 태그 조회 (사용 횟수 내림차순)
     */
    public List<TagResponse> getTagsByWorkspace(Workspace workspace) {
        List<Tag> tags = tagRepository.findTagsByWorkspaceOrderByUsage(workspace);

        return tags.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 워크스페이스의 태그를 이름순으로 조회
     */
    public List<TagResponse> getTagsByWorkspaceOrderByName(Workspace workspace) {
        List<Tag> tags = tagRepository.findTagsByWorkspaceOrderByName(workspace);

        return tags.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 워크스페이스 내에서 태그 이름으로 조회
     */
    public TagResponse getTagByWorkspaceAndName(Workspace workspace, String name) {
        Tag tag = tagRepository.findByWorkspaceAndName(workspace, name)
                .orElseThrow(() -> new TagNotFoundException(name));

        return TagResponse.from(tag);
    }

    /**
     * 워크스페이스의 인기 태그 조회
     */
    public List<TagSummaryResponse> getPopularTagsByWorkspace(Workspace workspace, int size) {
        Pageable pageable = PageRequest.of(0, size);
        List<Tag> tags = tagRepository.findPopularTagsByWorkspace(workspace, pageable);

        return tags.stream()
                .map(TagSummaryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 워크스페이스의 사용되지 않는 태그 조회
     */
    public List<TagResponse> getUnusedTagsByWorkspace(Workspace workspace) {
        List<Tag> tags = tagRepository.findUnusedTagsByWorkspace(workspace);

        return tags.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 워크스페이스의 시스템 태그 조회
     */
    public List<TagResponse> getSystemTagsByWorkspace(Workspace workspace) {
        List<Tag> tags = tagRepository.findSystemTagsByWorkspace(workspace);

        return tags.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 태그 이름으로 검색
     */
    public List<TagResponse> searchTagsByName(Workspace workspace, String keyword) {
        List<Tag> tags = tagRepository.searchTagsByName(workspace, keyword);

        return tags.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 색상별 태그 조회
     */
    public List<TagResponse> getTagsByColor(Workspace workspace, String color) {
        List<Tag> tags = tagRepository.findTagsByColor(workspace, color);

        return tags.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 페이지의 태그들 조회
     */
    public List<TagSummaryResponse> getTagsByPage(Long pageId) {
        List<Tag> tags = tagRepository.findTagsByPageId(pageId);

        return tags.stream()
                .map(TagSummaryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 여러 태그 이름으로 태그들 조회
     */
    public List<TagResponse> getTagsByNames(Workspace workspace, List<String> names) {
        List<Tag> tags = tagRepository.findTagsByWorkspaceAndNames(workspace, names);

        return tags.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용 횟수 이상의 태그 조회
     */
    public List<TagResponse> getTagsWithMinUsage(Workspace workspace, int minUsage) {
        List<Tag> tags = tagRepository.findTagsWithMinUsage(workspace, minUsage);

        return tags.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 최근 생성된 태그 조회
     */
    public List<TagResponse> getRecentTagsByWorkspace(Workspace workspace, int size) {
        Pageable pageable = PageRequest.of(0, size);
        List<Tag> tags = tagRepository.findRecentTagsByWorkspace(workspace, pageable);

        return tags.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 워크스페이스의 태그 수 조회
     */
    public long getTagCountByWorkspace(Workspace workspace) {
        return tagRepository.countTagsByWorkspace(workspace);
    }

    /**
     * 사용 중인 태그 수 조회
     */
    public long getUsedTagCountByWorkspace(Workspace workspace) {
        return tagRepository.countUsedTagsByWorkspace(workspace);
    }

    /**
     * 워크스페이스의 태그 통계 조회
     */
    public TagStatisticsResponse getTagStatistics(Workspace workspace) {
        Object[] statistics = tagRepository.getTagStatistics(workspace);

        if (statistics != null && statistics.length >= 4) {
            return new TagStatisticsResponse(
                    (Long) statistics[0],      // totalTags
                    (Long) statistics[1],      // usedTags
                    (Long) statistics[2],      // systemTags
                    (Double) statistics[3]     // averageUsage
            );
        }

        return new TagStatisticsResponse(0L, 0L, 0L, 0.0);
    }

    /**
     * 태그 이름 존재 여부 확인
     */
    public boolean existsTagName(Workspace workspace, String name) {
        return tagRepository.existsByWorkspaceAndName(workspace, name);
    }

    /**
     * 태그가 삭제 가능한지 확인
     */
    public boolean canDeleteTag(Long tagId) {
        Tag tag = tagRepository.findActiveTagById(tagId)
                .orElseThrow(() -> new TagNotFoundException(tagId));

        return tag.canDelete();
    }
}