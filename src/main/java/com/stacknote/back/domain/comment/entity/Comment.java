package com.stacknote.back.domain.comment.entity;

import com.stacknote.back.domain.page.entity.Page;
import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 댓글 엔티티
 * 페이지에 대한 댓글 및 대댓글 시스템
 */
@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comment_page", columnList = "page_id"),
        @Index(name = "idx_comment_author", columnList = "author_id"),
        @Index(name = "idx_comment_parent", columnList = "parent_id"),
        @Index(name = "idx_comment_created_at", columnList = "created_at")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Comment extends BaseEntity {

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content; // 댓글 내용

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    private Page page; // 댓글이 달린 페이지

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author; // 댓글 작성자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent; // 부모 댓글 (대댓글인 경우)

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> replies = new ArrayList<>(); // 대댓글들

    @Column(name = "is_edited", nullable = false)
    @Builder.Default
    private Boolean isEdited = false; // 수정 여부

    @Column(name = "likes_count", nullable = false)
    @Builder.Default
    private Integer likesCount = 0; // 좋아요 수

    @Column(name = "mentions") // JSON 형태로 멘션된 사용자 정보 저장
    private String mentions; // "@username" 형태의 멘션

    // ===== 비즈니스 로직 메서드 =====

    /**
     * 댓글 내용 수정
     */
    public void updateContent(String newContent) {
        if (newContent != null && !newContent.trim().isEmpty()) {
            this.content = newContent.trim();
            this.isEdited = true;
        }
    }

    /**
     * 대댓글 추가
     */
    public void addReply(Comment reply) {
        reply.setParent(this);
        this.replies.add(reply);
    }

    /**
     * 대댓글 제거
     */
    public void removeReply(Comment reply) {
        reply.setParent(null);
        this.replies.remove(reply);
    }

    /**
     * 부모 댓글 설정
     */
    public void setParent(Comment parent) {
        this.parent = parent;
    }

    /**
     * 좋아요 수 증가
     */
    public void incrementLikes() {
        this.likesCount++;
    }

    /**
     * 좋아요 수 감소
     */
    public void decrementLikes() {
        if (this.likesCount > 0) {
            this.likesCount--;
        }
    }

    /**
     * 최상위 댓글 여부 확인
     */
    public boolean isRootComment() {
        return this.parent == null;
    }

    /**
     * 댓글 깊이 계산 (최상위: 0, 대댓글: 1, 대대댓글: 2...)
     */
    public int getDepth() {
        int depth = 0;
        Comment current = this.parent;
        while (current != null) {
            depth++;
            current = current.parent;
        }
        return depth;
    }

    /**
     * 댓글 작성자 확인
     */
    public boolean isAuthor(User user) {
        return this.author.getId().equals(user.getId());
    }

    /**
     * 댓글 수정 가능 여부 확인
     */
    public boolean canEdit(User user) {
        return isAuthor(user) && !isDeleted();
    }

    /**
     * 댓글 삭제 가능 여부 확인
     */
    public boolean canDelete(User user) {
        return isAuthor(user) && !isDeleted();
    }

    /**
     * 대댓글 개수 반환
     */
    public int getReplyCount() {
        return (int) replies.stream()
                .filter(reply -> !reply.isDeleted())
                .count();
    }

    /**
     * 멘션 정보 설정
     */
    public void setMentions(String mentions) {
        this.mentions = mentions;
    }

    /**
     * 멘션 정보 반환
     */
    public String getMentions() {
        return this.mentions;
    }

    /**
     * 댓글이 멘션을 포함하고 있는지 확인
     */
    public boolean hasMentions() {
        return this.mentions != null && !this.mentions.trim().isEmpty();
    }
}