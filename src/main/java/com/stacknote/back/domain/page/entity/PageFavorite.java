package com.stacknote.back.domain.page.entity;

import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 페이지 즐겨찾기 엔티티
 * 사용자별 즐겨찾기 페이지 관리
 */
@Entity
@Table(name = "page_favorites",
        indexes = {
                @Index(name = "idx_page_favorite_user", columnList = "user_id"),
                @Index(name = "idx_page_favorite_page", columnList = "page_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "page_id"})
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PageFavorite extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    private Page page;

    // ===== 비즈니스 로직 메서드 =====

    /**
     * 즐겨찾기 생성
     */
    public static PageFavorite create(User user, Page page) {
        return PageFavorite.builder()
                .user(user)
                .page(page)
                .build();
    }
}