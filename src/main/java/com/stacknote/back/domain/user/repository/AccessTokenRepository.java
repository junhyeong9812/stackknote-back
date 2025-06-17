package com.stacknote.back.domain.user.repository;

import com.stacknote.back.domain.user.entity.AccessToken;
import com.stacknote.back.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 액세스 토큰 Repository
 */
@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {

    /**
     * 토큰으로 유효한 액세스 토큰 조회
     */
    @Query("SELECT at FROM AccessToken at WHERE at.token = :token AND at.isRevoked = false AND at.expiresAt > :now")
    Optional<AccessToken> findValidTokenByToken(@Param("token") String token, @Param("now") LocalDateTime now);

    /**
     * 사용자의 모든 유효한 액세스 토큰 조회
     */
    @Query("SELECT at FROM AccessToken at WHERE at.user = :user AND at.isRevoked = false AND at.expiresAt > :now")
    List<AccessToken> findValidTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * 사용자의 모든 액세스 토큰 철회
     */
    @Modifying
    @Query("UPDATE AccessToken at SET at.isRevoked = true WHERE at.user = :user AND at.isRevoked = false")
    int revokeAllByUser(@Param("user") User user);

    /**
     * 만료된 토큰들 삭제 (배치 작업용)
     */
    @Modifying
    @Query("DELETE FROM AccessToken at WHERE at.expiresAt < :expiredBefore")
    int deleteExpiredTokens(@Param("expiredBefore") LocalDateTime expiredBefore);

    /**
     * 사용자별 토큰 개수 조회
     */
    @Query("SELECT COUNT(at) FROM AccessToken at WHERE at.user = :user AND at.isRevoked = false AND at.expiresAt > :now")
    long countValidTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);
}