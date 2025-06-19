package com.stacknote.back.domain.page.entity;

import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 페이지 히스토리 엔티티
 * 페이지의 변경 이력을 추적하여 버전 관리 기능 제공
 */
@Entity
@Table(name = "page_histories", indexes = {
        @Index(name = "idx_page_history_page", columnList = "page_id"),
        @Index(name = "idx_page_history_version", columnList = "page_id, version"),
        @Index(name = "idx_page_history_modified_by", columnList = "modified_by"),
        @Index(name = "idx_page_history_created_at", columnList = "created_at")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PageHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    private Page page;

    @Column(name = "version", nullable = false)
    private Integer version; // 버전 번호

    @Column(name = "title", nullable = false, length = 200)
    private String title; // 해당 버전의 제목

    @Column(name = "content", columnDefinition = "TEXT")
    private String content; // 해당 버전의 콘텐츠

    @Column(name = "summary", length = 500)
    private String summary; // 해당 버전의 요약

    @Column(name = "icon", length = 10)
    private String icon; // 해당 버전의 아이콘

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl; // 해당 버전의 커버 이미지

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modified_by", nullable = false)
    private User modifiedBy; // 수정한 사용자

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false)
    private ChangeType changeType; // 변경 유형

    @Column(name = "change_description", length = 1000)
    private String changeDescription; // 변경 설명

    @Column(name = "content_size", nullable = false)
    @Builder.Default
    private Long contentSize = 0L; // 콘텐츠 크기 (바이트)

    // ===== 비즈니스 로직 메서드 =====

    /**
     * 현재 페이지 상태로부터 히스토리 생성
     */
    public static PageHistory createFromPage(Page page, User modifiedBy, ChangeType changeType, String changeDescription) {
        return PageHistory.builder()
                .page(page)
                .version(getNextVersion(page))
                .title(page.getTitle())
                .content(page.getContent())
                .summary(page.getSummary())
                .icon(page.getIcon())
                .coverImageUrl(page.getCoverImageUrl())
                .modifiedBy(modifiedBy)
                .changeType(changeType)
                .changeDescription(changeDescription)
                .contentSize(calculateContentSize(page.getContent()))
                .build();
    }

    /**
     * 다음 버전 번호 계산 (실제로는 Repository에서 조회해야 함)
     */
    private static Integer getNextVersion(Page page) {
        // 실제 구현에서는 Repository를 통해 마지막 버전 번호를 조회
        return 1; // 임시로 1 반환
    }

    /**
     * 콘텐츠 크기 계산
     */
    private static Long calculateContentSize(String content) {
        if (content == null) {
            return 0L;
        }
        return (long) content.getBytes().length;
    }

    /**
     * 변경 내용이 있는지 확인
     */
    public boolean hasContentChanges() {
        return changeType == ChangeType.CONTENT_UPDATED ||
                changeType == ChangeType.TITLE_UPDATED ||
                changeType == ChangeType.MAJOR_UPDATE;
    }

    /**
     * 메타데이터만 변경되었는지 확인
     */
    public boolean isMetadataOnly() {
        return changeType == ChangeType.METADATA_UPDATED;
    }

    /**
     * 변경 유형
     */
    public enum ChangeType {
        CREATED,           // 페이지 생성
        TITLE_UPDATED,     // 제목 변경
        CONTENT_UPDATED,   // 콘텐츠 변경
        METADATA_UPDATED,  // 메타데이터 변경 (아이콘, 커버 이미지 등)
        STRUCTURE_CHANGED, // 구조 변경 (부모-자식 관계)
        STATUS_CHANGED,    // 상태 변경 (공개/비공개, 잠금 등)
        MAJOR_UPDATE,      // 주요 업데이트
        RESTORED          // 이전 버전으로 복원
    }
}