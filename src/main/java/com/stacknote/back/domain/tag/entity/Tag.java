package com.stacknote.back.domain.tag.entity;

import com.stacknote.back.domain.workspace.entity.Workspace;
import com.stacknote.back.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 태그 엔티티
 * 워크스페이스 내에서 페이지를 분류하는 태그 시스템
 */
@Entity
@Table(name = "tags",
        indexes = {
                @Index(name = "idx_tag_workspace", columnList = "workspace_id"),
                @Index(name = "idx_tag_name", columnList = "name"),
                @Index(name = "idx_tag_usage_count", columnList = "usage_count")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tag_workspace_name", columnNames = {"workspace_id", "name"})
        })
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Tag extends BaseEntity {

    @Column(name = "name", nullable = false, length = 50)
    private String name; // 태그 이름

    @Column(name = "color", length = 7)
    private String color; // 태그 색상 (#RRGGBB 형식)

    @Column(name = "description", length = 255)
    private String description; // 태그 설명

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace; // 소속 워크스페이스

    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PageTag> pageTags = new ArrayList<>(); // 페이지-태그 연관관계

    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private Integer usageCount = 0; // 사용 횟수

    @Column(name = "is_system_tag", nullable = false)
    @Builder.Default
    private Boolean isSystemTag = false; // 시스템 태그 여부 (삭제 불가)

    // ===== 비즈니스 로직 메서드 =====

    /**
     * 태그 정보 수정
     */
    public void updateTag(String name, String color, String description) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
        this.color = color;
        this.description = description;
    }

    /**
     * 태그 사용 횟수 증가
     */
    public void incrementUsage() {
        this.usageCount++;
    }

    /**
     * 태그 사용 횟수 감소
     */
    public void decrementUsage() {
        if (this.usageCount > 0) {
            this.usageCount--;
        }
    }

    /**
     * 시스템 태그로 설정
     */
    public void markAsSystemTag() {
        this.isSystemTag = true;
    }

    /**
     * 태그 삭제 가능 여부 확인
     */
    public boolean canDelete() {
        return !this.isSystemTag && this.usageCount == 0;
    }

    /**
     * 태그가 사용 중인지 확인
     */
    public boolean isInUse() {
        return this.usageCount > 0;
    }

    /**
     * 기본 색상 설정
     */
    public void setDefaultColor() {
        if (this.color == null || this.color.trim().isEmpty()) {
            this.color = generateDefaultColor();
        }
    }

    /**
     * 태그 이름의 해시값을 기반으로 기본 색상 생성
     */
    private String generateDefaultColor() {
        String[] defaultColors = {
                "#3498db", "#e74c3c", "#2ecc71", "#f39c12",
                "#9b59b6", "#1abc9c", "#34495e", "#e67e22"
        };

        int index = Math.abs(this.name.hashCode()) % defaultColors.length;
        return defaultColors[index];
    }

    /**
     * 색상 유효성 검증
     */
    public static boolean isValidColor(String color) {
        if (color == null) return true; // null은 허용 (기본 색상 사용)
        return color.matches("^#[0-9A-Fa-f]{6}$");
    }

    /**
     * 태그 이름 유효성 검증
     */
    public static boolean isValidName(String name) {
        return name != null &&
                name.trim().length() >= 1 &&
                name.trim().length() <= 50 &&
                !name.trim().contains(","); // 쉼표는 구분자로 사용하므로 제외
    }
}