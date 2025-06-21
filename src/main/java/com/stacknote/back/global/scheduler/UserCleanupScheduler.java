package com.stacknote.back.global.scheduler;

import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.domain.user.repository.AccessTokenRepository;
import com.stacknote.back.domain.user.repository.RefreshTokenRepository;
import com.stacknote.back.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자 계정 정리 스케줄러
 * 비활성화된 지 3개월이 지난 계정들을 자동으로 삭제
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserCleanupScheduler {

    private final UserRepository userRepository;
    private final AccessTokenRepository accessTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * 매일 자정에 비활성화된 계정 정리 작업 실행
     * cron = "0 0 0 * * ?" (초 분 시 일 월 요일)
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanupDeactivatedAccounts() {
        log.info("비활성화된 계정 정리 작업 시작");

        try {
            // 3개월 이상 비활성화된 계정 조회
            LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
            List<User> usersToDelete = userRepository.findUsersDeactivatedBefore(threeMonthsAgo);

            if (usersToDelete.isEmpty()) {
                log.info("삭제할 비활성화 계정이 없습니다.");
                return;
            }

            log.info("삭제 대상 계정 수: {}", usersToDelete.size());

            int deletedCount = 0;
            for (User user : usersToDelete) {
                try {
                    // 관련 토큰들 먼저 삭제
                    deleteUserTokens(user);

                    // 사용자 계정 완전 삭제
                    userRepository.delete(user);
                    deletedCount++;

                    log.info("계정 삭제 완료: {} (비활성화 날짜: {})",
                            user.getEmail(), user.getDeactivatedAt());

                } catch (Exception e) {
                    log.error("계정 삭제 실패: {} - {}", user.getEmail(), e.getMessage(), e);
                }
            }

            log.info("비활성화된 계정 정리 작업 완료 - 삭제된 계정 수: {} / {}",
                    deletedCount, usersToDelete.size());

        } catch (Exception e) {
            log.error("비활성화된 계정 정리 작업 중 오류 발생", e);
        }
    }

    /**
     * 사용자의 모든 토큰 삭제
     */
    private void deleteUserTokens(User user) {
        try {
            // 액세스 토큰 삭제
            accessTokenRepository.deleteAllByUser(user);

            // 리프레시 토큰 삭제
            refreshTokenRepository.deleteAllByUser(user);

            log.debug("사용자 토큰 삭제 완료: {}", user.getEmail());
        } catch (Exception e) {
            log.warn("사용자 토큰 삭제 중 오류 발생: {} - {}", user.getEmail(), e.getMessage());
        }
    }

    /**
     * 수동으로 정리 작업을 실행하는 메서드 (관리자용)
     */
    @Transactional
    public void manualCleanup() {
        log.info("수동 계정 정리 작업 실행");
        cleanupDeactivatedAccounts();
    }
}