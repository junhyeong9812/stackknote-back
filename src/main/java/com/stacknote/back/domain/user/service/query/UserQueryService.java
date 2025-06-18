package com.stacknote.back.domain.user.service.query;

import com.stacknote.back.domain.user.dto.response.UserProfileResponse;
import com.stacknote.back.domain.user.dto.response.UserResponse;
import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.domain.user.exception.UserNotFoundException;
import com.stacknote.back.domain.user.repository.UserRepository;
import com.stacknote.back.global.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 관련 쿼리 서비스
 * 사용자 조회, 검색 등의 읽기 전용 작업 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserRepository userRepository;

    /**
     * 사용자 프로필 조회
     */
    public UserProfileResponse getUserProfile(Long userId) {
        log.debug("사용자 프로필 조회: {}", userId);

        User user = userRepository.findActiveUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        return UserProfileResponse.from(user);
    }

    /**
     * 사용자 기본 정보 조회
     */
    public UserResponse getUserById(Long userId) {
        log.debug("사용자 정보 조회: {}", userId);

        User user = userRepository.findActiveUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        return UserResponse.from(user);
    }

    /**
     * 이메일로 사용자 조회
     */
    public UserResponse getUserByEmail(String email) {
        log.debug("이메일로 사용자 조회: {}", email);

        User user = userRepository.findActiveUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        return UserResponse.from(user);
    }

    /**
     * 사용자명으로 사용자 조회
     */
    public UserResponse getUserByUsername(String username) {
        log.debug("사용자명으로 사용자 조회: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        return UserResponse.from(user);
    }

    /**
     * 이메일 중복 확인
     */
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 사용자명 중복 확인
     */
    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 모든 사용자 목록 조회 (관리자용)
     */
    public PageResponse<UserResponse> getAllUsers(Pageable pageable) {
        log.debug("전체 사용자 목록 조회 - 페이지: {}, 크기: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<User> userPage = userRepository.findAll(pageable);
        Page<UserResponse> responsePage = userPage.map(UserResponse::from);

        return PageResponse.of(responsePage);
    }

    /**
     * 활성 사용자 수 조회
     */
    public long getActiveUserCount() {
        // 이 메서드는 UserRepository에 추가 쿼리 메서드가 필요합니다
        // 임시로 전체 사용자 수를 반환합니다
        return userRepository.count();
    }
}