package com.stacknote.back.global.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * JWT 토큰 유틸리티 클래스
 * 토큰 생성, 파싱, 검증 기능 제공
 */
@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessTokenValidityInMinutes;
    private final long refreshTokenValidityInMinutes;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity:30}") long accessTokenValidityInMinutes,
            @Value("${jwt.refresh-token-validity:120}") long refreshTokenValidityInMinutes
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenValidityInMinutes = accessTokenValidityInMinutes;
        this.refreshTokenValidityInMinutes = refreshTokenValidityInMinutes;
    }

    /**
     * 액세스 토큰 생성
     */
    public String generateAccessToken(String userEmail, Long userId) {
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(accessTokenValidityInMinutes);

        return Jwts.builder()
                .setSubject(userEmail)
                .claim("userId", userId)
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(Date.from(expiryDate.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 리프레시 토큰 생성
     */
    public String generateRefreshToken(String userEmail, Long userId) {
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(refreshTokenValidityInMinutes);

        return Jwts.builder()
                .setSubject(userEmail)
                .claim("userId", userId)
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(Date.from(expiryDate.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 토큰에서 사용자 이메일 추출
     */
    public String getEmailFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * 토큰에서 사용자 ID 추출
     */
    public Long getUserIdFromToken(String token) {
        return getClaimsFromToken(token).get("userId", Long.class);
    }

    /**
     * 토큰 타입 확인 (access/refresh)
     */
    public String getTokenType(String token) {
        return getClaimsFromToken(token).get("type", String.class);
    }

    /**
     * 토큰 만료일시 확인
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimsFromToken(token).getExpiration();
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰 만료 여부 확인
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    /**
     * 액세스 토큰 만료 시간 계산
     */
    public LocalDateTime getAccessTokenExpiryTime() {
        return LocalDateTime.now().plusMinutes(accessTokenValidityInMinutes);
    }

    /**
     * 리프레시 토큰 만료 시간 계산
     */
    public LocalDateTime getRefreshTokenExpiryTime() {
        return LocalDateTime.now().plusMinutes(refreshTokenValidityInMinutes);
    }

    /**
     * 토큰에서 Claims 추출
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}