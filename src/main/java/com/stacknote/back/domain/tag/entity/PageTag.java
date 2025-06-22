package com.stacknote.back.domain.tag.entity;

import com.stacknote.back.domain.page.entity.Page;
import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 페이지-태그 연관관계 엔티티
 * 페이지와 태그 간의 다대다 관계를 관리
 */
@Entity
@Table(name = "page_tags",
        indexes = {
                @Index(name = "idx_page_tag_page", columnList = "page_id"),
                @Index(name = "idx_page_tag_tag", columnList = "tag_id"),
                @Index(name = "idx_page_tag_created_by", columnList = "created_by_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_page_tag", columnNames = {"page_id", "tag_id"})
        })
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PageTag extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    private Page page; // 태그가 붙은 페이지

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag; // 페이지에 붙은 태그

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy; // 태그를 붙인 사용자

    @Column(name = "position")
    private Integer position; // 태그 표시 순서

    // ===== 비즈니스 로직 메서드 =====

    /**
     * 페이지-태그 연관관계 생성
     */
    public static PageTag create(Page page, Tag tag, User createdBy) {
        return PageTag.builder()
                .page(page)
                .tag(tag)
                .createdBy(createdBy)
                .build();
    }

    /**
     * 태그 위치 설정
     */
    public void setPosition(Integer position) {
        this.position = position;
    }

    /**
     * 태그를 붙인 사용자인지 확인
     */
    public boolean isCreatedBy(User user) {
        return this.createdBy.getId().equals(user.getId());
    }

    /**
     * 같은 워크스페이스 내의 태그인지 확인
     */
    public boolean isSameWorkspace() {
        return this.page.getWorkspace().getId().equals(this.tag.getWorkspace().getId());
    }
}