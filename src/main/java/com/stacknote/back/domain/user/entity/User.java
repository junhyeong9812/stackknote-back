package com.stacknote.back.domain.user.entity;

import com.stacknote.back.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

/**
 * 사용자 엔티티
 * Spring Security UserDetails 인터페이스 구현
 */
@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseEntity implements UserDetails {

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @Column(name = "is_email_verified", nullable = false)
    @Builder.Default
    private Boolean isEmailVerified = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 계정 비활성화 날짜
     * 비활성화된 계정이 3개월 후 자동 삭제되도록 하기 위한 필드
     */
    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;

    // ===== 비즈니스 로직 메서드 =====

    /**
     * 사용자 정보 업데이트
     */
    public void updateProfile(String username, String profileImageUrl) {
        if (username != null && !username.trim().isEmpty()) {
            this.username = username.trim();
        }
        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }
    }

    /**
     * 비밀번호 변경
     */
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    /**
     * 이메일 인증 완료
     */
    public void verifyEmail() {
        this.isEmailVerified = true;
    }

    /**
     * 계정 비활성화
     */
    public void deactivate() {
        this.isActive = false;
        this.deactivatedAt = LocalDateTime.now();
    }

    /**
     * 계정 활성화
     */
    public void activate() {
        this.isActive = true;
        this.deactivatedAt = null; // 비활성화 날짜 삭제
    }

    /**
     * 비활성화 후 경과 시간 확인 (개월 단위)
     */
    public long getMonthsSinceDeactivation() {
        if (deactivatedAt == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.MONTHS.between(deactivatedAt, LocalDateTime.now());
    }

    /**
     * 3개월 이상 비활성화된 계정인지 확인
     */
    public boolean isEligibleForDeletion() {
        return deactivatedAt != null && getMonthsSinceDeactivation() >= 3;
    }

    // ===== UserDetails 구현 =====

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email; // 로그인 시 이메일을 username으로 사용
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive && !isDeleted();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive && !isDeleted();
    }

    // ===== 사용자 역할 Enum =====
    public enum Role {
        USER,      // 일반 사용자
        ADMIN      // 관리자
    }
}