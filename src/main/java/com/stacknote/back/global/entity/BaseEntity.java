package com.stacknote.back.global.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * ID와 소프트 삭제 기능을 포함한 기본 엔티티
 * BaseTimeEntity를 상속받아 시간 정보도 포함
 */
@MappedSuperclass
@Getter
public abstract class BaseEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 소프트 삭제 처리
     */
    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * 삭제 복구
     */
    public void restore() {
        this.deletedAt = null;
    }
}