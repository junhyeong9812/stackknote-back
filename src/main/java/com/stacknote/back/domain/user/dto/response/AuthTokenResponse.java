package com.stacknote.back.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 인증 토큰 응답 DTO
 * 로그인 성공 시 토큰 정보 제공
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

    /**
     * 토큰 응답 생성
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