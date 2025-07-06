package com.stacknote.back.domain.page.entity;

import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 페이지 방문 기록 엔티티
 * 최근 방문 페이지 추적을 위한 엔티티
 */
@Entity
@Table(name = "page_visits",
        indexes = {
                @Index(name = "idx_page_visit_user", columnList = "user_id"),
                @Index(name = "idx_page_visit_page", columnList = "page_id"),
                @Index(name = "idx_page_visit_visited_at", columnList = "visited_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "page_id"})
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PageVisit extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    private Page page;

    @Column(name = "visited_at", nullable = false)
    private LocalDateTime visitedAt;

    @Column(name = "visit_count", nullable = false)
    @Builder.Default
    private Long visitCount = 1L;

    // ===== 비즈니스 로직 메서드 =====

    /**
     * 방문 기록 업데이트
     */
    public void updateVisit() {
        this.visitedAt = LocalDateTime.now();
        this.visitCount++;
    }

    /**
     * 페이지 방문 기록 생성
     */
    public static PageVisit create(User user, Page page) {
        return PageVisit.builder()
                .user(user)
                .page(page)
                .visitedAt(LocalDateTime.now())
                .visitCount(1L)
                .build();
    }
}