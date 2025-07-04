package com.stacknote.back.global.config;

import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 애플리케이션 시작 시 초기 데이터를 생성하는 컴포넌트
 * 관리자 계정과 테스트용 사용자 계정을 생성합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${data.init.enabled:false}")
    private boolean initEnabled;

    @Value("${data.init.admin-count:1}")
    private int adminCount;

    @Value("${data.init.user-count:10}")
    private int dummyUserCount;

    // 환경변수에서 관리자 정보 읽기 (보안)
    @Value("${ADMIN_EMAIL:}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    @Value("${ADMIN_USERNAME:}")
    private String adminUsername;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (!initEnabled) {
            log.info("데이터 초기화가 비활성화되어 있습니다.");
            return;
        }

        log.info("데이터 초기화를 시작합니다...");

        // 관리자 계정 생성
        createAdminUsers();

        // 더미 사용자 생성
        createDummyUsers();

        log.info("데이터 초기화가 완료되었습니다.");
    }

    /**
     * 관리자 계정 생성
     * 환경변수가 설정되어 있으면 사용하고, 없으면 기본 개발용 계정 생성
     */
    private void createAdminUsers() {
        for (int i = 1; i <= adminCount; i++) {
            String email;
            String password;
            String username;

            if (i == 1 && isValidAdminConfig()) {
                // 첫 번째 관리자는 환경변수 사용
                email = adminEmail;
                password = adminPassword;
                username = adminUsername;
            } else {
                // 추가 관리자나 환경변수가 없는 경우 개발용 기본값
                email = i == 1 ? "admin@stacknote.com" : String.format("admin%d@stacknote.com", i);
                password = "admin123!";
                username = i == 1 ? "StackNote Admin" : String.format("관리자%d", i);
            }

            if (userRepository.existsByEmail(email)) {
                log.info("관리자 계정이 이미 존재합니다: {}", email);
                continue;
            }

            User admin = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .username(username)
                    .role(User.Role.ADMIN)
                    .isEmailVerified(true)
                    .isActive(true)
                    .build();

            userRepository.save(admin);
            log.info("관리자 계정이 생성되었습니다: {} ({})", username, email);
        }
    }

    /**
     * 더미 사용자 생성
     */
    private void createDummyUsers() {
        List<User> dummyUsers = new ArrayList<>();

        for (int i = 1; i <= dummyUserCount; i++) {
            String email = String.format("user%d@stacknote.com", i);

            // 이미 존재하는 사용자는 건너뛰기
            if (userRepository.existsByEmail(email)) {
                continue;
            }

            User user = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode("password123!"))
                    .username(String.format("사용자%d", i))
                    .role(User.Role.USER)
                    .isEmailVerified(i % 3 == 0) // 3명 중 1명은 이메일 미인증
                    .isActive(i % 5 != 0) // 5명 중 1명은 비활성화
                    .build();

            // 비활성화된 사용자는 비활성화 처리
            if (!user.getIsActive()) {
                user.deactivate();
            }

            dummyUsers.add(user);
        }

        if (!dummyUsers.isEmpty()) {
            userRepository.saveAll(dummyUsers);
            log.info("더미 사용자 {}명이 생성되었습니다.", dummyUsers.size());
        }

        // 생성된 사용자 통계 출력
        printUserStatistics();
    }

    /**
     * 관리자 환경변수 설정이 유효한지 확인
     */
    private boolean isValidAdminConfig() {
        return adminEmail != null && !adminEmail.trim().isEmpty() &&
                adminPassword != null && !adminPassword.trim().isEmpty() &&
                adminUsername != null && !adminUsername.trim().isEmpty();
    }

    /**
     * 사용자 통계 출력
     */
    private void printUserStatistics() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countActiveUsers();
        long inactiveUsers = userRepository.countInactiveUsers();

        log.info("=== 사용자 통계 ===");
        log.info("전체 사용자: {}명", totalUsers);
        log.info("활성 사용자: {}명", activeUsers);
        log.info("비활성 사용자: {}명", inactiveUsers);
        log.info("===============");
    }
}