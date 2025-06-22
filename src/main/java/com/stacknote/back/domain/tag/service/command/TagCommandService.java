package com.stacknote.back.domain.tag.service.command;

import com.stacknote.back.domain.page.entity.Page;
import com.stacknote.back.domain.page.repository.PageRepository;
import com.stacknote.back.domain.tag.dto.request.TagAddToPageRequest;
import com.stacknote.back.domain.tag.dto.request.TagCreateRequest;
import com.stacknote.back.domain.tag.dto.request.TagUpdateRequest;
import com.stacknote.back.domain.tag.dto.response.TagResponse;
import com.stacknote.back.domain.tag.entity.PageTag;
import com.stacknote.back.domain.tag.entity.Tag;
import com.stacknote.back.domain.tag.exception.DuplicateTagException;
import com.stacknote.back.domain.tag.exception.TagInUseException;
import com.stacknote.back.domain.tag.exception.TagNotFoundException;
import com.stacknote.back.domain.tag.repository.PageTagRepository;
import com.stacknote.back.domain.tag.repository.TagRepository;
import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.domain.workspace.entity.Workspace;
import com.stacknote.back.domain.workspace.repository.WorkspaceRepository;
import com.stacknote.back.global.exception.custom.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 태그 명령 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TagCommandService {

    private final TagRepository tagRepository;
    private final PageTagRepository pageTagRepository;
    private final PageRepository pageRepository;
    private final WorkspaceRepository workspaceRepository;

    /**
     * 태그 생성
     */
    public TagResponse createTag(TagCreateRequest request) {
        // 워크스페이스 존재 확인
        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> new EntityNotFoundException("워크스페이스를 찾을 수 없습니다."));

        // 태그 이름 중복 확인
        if (tagRepository.existsByWorkspaceAndName(workspace, request.getName())) {
            throw new DuplicateTagException(request.getName());
        }

        // 태그 이름 유효성 검증
        if (!Tag.isValidName(request.getName())) {
            throw new IllegalArgumentException("유효하지 않은 태그 이름입니다.");
        }

        // 색상 유효성 검증
        if (request.getColor() != null && !Tag.isValidColor(request.getColor())) {
            throw new IllegalArgumentException("유효하지 않은 색상 형식입니다.");
        }

        Tag tag = Tag.builder()
                .name(request.getName())
                .color(request.getColor())
                .description(request.getDescription())
                .workspace(workspace)
                .build();

        // 기본 색상 설정
        tag.setDefaultColor();

        Tag savedTag = tagRepository.save(tag);

        log.info("태그 생성 완료 - ID: {}, 이름: {}, 워크스페이스: {}",
                savedTag.getId(), savedTag.getName(), workspace.getId());

        return TagResponse.from(savedTag);
    }

    /**
     * 태그 수정
     */
    public TagResponse updateTag(Long tagId, TagUpdateRequest request) {
        Tag tag = tagRepository.findActiveTagById(tagId)
                .orElseThrow(() -> new TagNotFoundException(tagId));

        // 시스템 태그는 수정 불가
        if (tag.getIsSystemTag()) {
            throw new IllegalArgumentException("시스템 태그는 수정할 수 없습니다.");
        }

        // 태그 이름 중복 확인 (변경하는 경우)
        if (request.getName() != null && !request.getName().equals(tag.getName())) {
            if (tagRepository.existsByWorkspaceAndName(tag.getWorkspace(), request.getName())) {
                throw new DuplicateTagException(request.getName());
            }

            // 태그 이름 유효성 검증
            if (!Tag.isValidName(request.getName())) {
                throw new IllegalArgumentException("유효하지 않은 태그 이름입니다.");
            }
        }

        // 색상 유효성 검증
        if (request.getColor() != null && !Tag.isValidColor(request.getColor())) {
            throw new IllegalArgumentException("유효하지 않은 색상 형식입니다.");
        }

        tag.updateTag(request.getName(), request.getColor(), request.getDescription());

        log.info("태그 수정 완료 - ID: {}, 이름: {}", tagId, tag.getName());

        return TagResponse.from(tag);
    }

    /**
     * 태그 삭제
     */
    public void deleteTag(Long tagId) {
        Tag tag = tagRepository.findActiveTagById(tagId)
                .orElseThrow(() -> new TagNotFoundException(tagId));

        // 삭제 가능 여부 확인
        if (!tag.canDelete()) {
            if (tag.getIsSystemTag()) {
                throw new IllegalArgumentException("시스템 태그는 삭제할 수 없습니다.");
            }
            if (tag.isInUse()) {
                throw new TagInUseException(tag.getName());
            }
        }

        // 모든 페이지-태그 연관관계 삭제
        pageTagRepository.deleteByTag(tag);

        // 태그 소프트 삭제
        tag.markAsDeleted();

        log.info("태그 삭제 완료 - ID: {}, 이름: {}", tagId, tag.getName());
    }

    /**
     * 페이지에 태그 추가
     */
    public List<TagResponse> addTagsToPage(TagAddToPageRequest request, User user) {
        // 페이지 존재 확인
        Page page = pageRepository.findById(request.getPageId())
                .orElseThrow(() -> new EntityNotFoundException("페이지를 찾을 수 없습니다."));

        List<TagResponse> addedTags = new ArrayList<>();

        for (String tagName : request.getTagNames()) {
            // 태그 찾기 또는 생성
            Tag tag = findOrCreateTag(page.getWorkspace(), tagName);

            // 이미 페이지에 태그가 있는지 확인
            if (!pageTagRepository.existsByPageAndTag(page, tag)) {
                // 페이지-태그 연관관계 생성
                PageTag pageTag = PageTag.create(page, tag, user);

                // 위치 설정
                Integer maxPosition = pageTagRepository.findMaxPositionByPage(page);
                pageTag.setPosition(maxPosition != null ? maxPosition + 1 : 1);

                pageTagRepository.save(pageTag);

                // 태그 사용 횟수 증가
                tag.incrementUsage();

                addedTags.add(TagResponse.from(tag));

                log.debug("페이지에 태그 추가 - 페이지 ID: {}, 태그: {}", page.getId(), tagName);
            }
        }

        log.info("페이지 태그 추가 완료 - 페이지 ID: {}, 추가된 태그 수: {}",
                request.getPageId(), addedTags.size());

        return addedTags;
    }

    /**
     * 페이지에서 태그 제거
     */
    public void removeTagFromPage(Long pageId, Long tagId, User user) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new EntityNotFoundException("페이지를 찾을 수 없습니다."));

        Tag tag = tagRepository.findActiveTagById(tagId)
                .orElseThrow(() -> new TagNotFoundException(tagId));

        // 페이지-태그 연관관계 삭제
        pageTagRepository.deleteByPageAndTag(page, tag);

        // 태그 사용 횟수 감소
        tag.decrementUsage();

        log.info("페이지에서 태그 제거 완료 - 페이지 ID: {}, 태그 ID: {}", pageId, tagId);
    }

    /**
     * 페이지의 모든 태그 제거
     */
    public void removeAllTagsFromPage(Long pageId) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new EntityNotFoundException("페이지를 찾을 수 없습니다."));

        // 페이지의 모든 태그 조회하여 사용 횟수 감소
        List<Tag> tags = tagRepository.findTagsByPageId(pageId);
        for (Tag tag : tags) {
            tag.decrementUsage();
        }

        // 모든 페이지-태그 연관관계 삭제
        pageTagRepository.deleteByPage(page);

        log.info("페이지의 모든 태그 제거 완료 - 페이지 ID: {}", pageId);
    }

    /**
     * 태그 사용 횟수 업데이트
     */
    public void updateTagUsageCount(Long tagId, int increment) {
        tagRepository.updateUsageCount(tagId, increment);
        log.debug("태그 사용 횟수 업데이트 - 태그 ID: {}, 증감: {}", tagId, increment);
    }

    /**
     * 워크스페이스 삭제 시 모든 태그 삭제
     */
    public void deleteTagsByWorkspace(Workspace workspace) {
        int deletedCount = tagRepository.softDeleteTagsByWorkspace(workspace);
        log.info("워크스페이스 태그 삭제 완료 - 워크스페이스 ID: {}, 삭제된 태그 수: {}",
                workspace.getId(), deletedCount);
    }

    /**
     * 태그 찾기 또는 생성
     */
    private Tag findOrCreateTag(Workspace workspace, String tagName) {
        return tagRepository.findByWorkspaceAndName(workspace, tagName)
                .orElseGet(() -> {
                    Tag newTag = Tag.builder()
                            .name(tagName)
                            .workspace(workspace)
                            .build();

                    newTag.setDefaultColor();

                    Tag saved = tagRepository.save(newTag);
                    log.debug("새 태그 자동 생성 - 이름: {}, 워크스페이스: {}",
                            tagName, workspace.getId());

                    return saved;
                });
    }
}