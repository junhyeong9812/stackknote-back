package com.stacknote.back.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 인증 토큰 응답 DTO
 * 로그인 성공 시 토큰 정보 제공 (실제 토큰값은 쿠키에 저장)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokenResponse {

    private String tokenType;           // "Bearer"
    private LocalDateTime accessTokenExpiresAt;   // 액세스 토큰 만료 시간
    private LocalDateTime refreshTokenExpiresAt;  // 리프레시 토큰 만료 시간
    private UserResponse user;          // 사용자 정보

    // 내부적으로 사용할 실제 토큰값들 (JSON 응답에는 포함되지 않음)
    @Builder.Default
    private transient String accessToken = null;
    @Builder.Default
    private transient String refreshToken = null;

    /**
     * 토큰 응답 생성 (토큰값 포함)
     */
    public static AuthTokenResponse of(
            String accessToken,
            String refreshToken,
            LocalDateTime accessTokenExpiresAt,
            LocalDateTime refreshTokenExpiresAt,
            UserResponse user
    ) {
        return AuthTokenResponse.builder()
                .tokenType("Bearer")
                .accessTokenExpiresAt(accessTokenExpiresAt)
                .refreshTokenExpiresAt(refreshTokenExpiresAt)
                .user(user)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 토큰 응답 생성 (기존 메서드 - 하위 호환성)
     */
    public static AuthTokenResponse of(
            LocalDateTime accessTokenExpiresAt,
            LocalDateTime refreshTokenExpiresAt,
            UserResponse user
    ) {
        return AuthTokenResponse.builder()
                .tokenType("Bearer")
                .accessTokenExpiresAt(accessTokenExpiresAt)
                .refreshTokenExpiresAt(refreshTokenExpiresAt)
                .user(user)
                .build();
    }
}