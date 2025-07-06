package com.stacknote.back.domain.page.service.command;

import com.stacknote.back.domain.page.entity.Page;
import com.stacknote.back.domain.page.entity.PageFavorite;
import com.stacknote.back.domain.page.entity.PageVisit;
import com.stacknote.back.domain.page.exception.PageAccessDeniedException;
import com.stacknote.back.domain.page.exception.PageNotFoundException;
import com.stacknote.back.domain.page.repository.PageFavoriteRepository;
import com.stacknote.back.domain.page.repository.PageRepository;
import com.stacknote.back.domain.page.repository.PageVisitRepository;
import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.domain.workspace.entity.Workspace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 페이지 액션 서비스
 * 즐겨찾기, 방문 기록 등 페이지 관련 부가 기능 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PageActionService {

    private final PageRepository pageRepository;
    private final PageFavoriteRepository pageFavoriteRepository;
    private final PageVisitRepository pageVisitRepository;

    /**
     * 페이지 즐겨찾기 토글
     * @return true: 즐겨찾기 추가됨, false: 즐겨찾기 제거됨
     */
    public boolean toggleFavorite(Long pageId, User currentUser) {
        log.info("페이지 즐겨찾기 토글: 페이지 {}, 사용자 {}", pageId, currentUser.getId());

        // 페이지 조회 및 권한 확인
        Page page = pageRepository.findActivePageById(pageId)
                .orElseThrow(() -> new PageNotFoundException("페이지를 찾을 수 없습니다."));

        // 페이지 접근 권한 확인
        validatePageAccess(page, currentUser);

        // 기존 즐겨찾기 확인
        Optional<PageFavorite> existingFavorite = pageFavoriteRepository.findByUserAndPage(currentUser, page);

        if (existingFavorite.isPresent()) {
            // 즐겨찾기 제거
            pageFavoriteRepository.delete(existingFavorite.get());
            log.info("즐겨찾기 제거됨: 페이지 {}, 사용자 {}", pageId, currentUser.getId());
            return false;
        } else {
            // 즐겨찾기 추가
            PageFavorite favorite = PageFavorite.create(currentUser, page);
            pageFavoriteRepository.save(favorite);
            log.info("즐겨찾기 추가됨: 페이지 {}, 사용자 {}", pageId, currentUser.getId());
            return true;
        }
    }

    /**
     * 페이지 방문 기록
     */
    public void recordPageVisit(Long pageId, User currentUser) {
        log.debug("페이지 방문 기록: 페이지 {}, 사용자 {}", pageId, currentUser.getId());

        // 페이지 조회 및 권한 확인
        Page page = pageRepository.findActivePageById(pageId)
                .orElseThrow(() -> new PageNotFoundException("페이지를 찾을 수 없습니다."));

        // 페이지 접근 권한 확인
        validatePageAccess(page, currentUser);

        // 기존 방문 기록 확인
        Optional<PageVisit> existingVisit = pageVisitRepository.findByUserAndPage(currentUser, page);

        if (existingVisit.isPresent()) {
            // 방문 기록 업데이트
            PageVisit visit = existingVisit.get();
            visit.updateVisit();
            pageVisitRepository.save(visit);
        } else {
            // 새 방문 기록 생성
            PageVisit visit = PageVisit.create(currentUser, page);
            pageVisitRepository.save(visit);
        }

        // 페이지 조회수 증가
        pageRepository.incrementViewCount(pageId);

        log.debug("페이지 방문 기록 완료: 페이지 {}", pageId);
    }

    /**
     * 페이지 접근 권한 검증
     */
    private void validatePageAccess(Page page, User user) {
        Workspace workspace = page.getWorkspace();

        // 공개 페이지인 경우 접근 허용
        if (page.getIsPublished() && workspace.getVisibility() == Workspace.Visibility.PUBLIC) {
            return;
        }

        // 워크스페이스 멤버 확인
        if (!workspace.isMember(user)) {
            throw new PageAccessDeniedException("페이지 접근 권한이 없습니다.");
        }
    }
}