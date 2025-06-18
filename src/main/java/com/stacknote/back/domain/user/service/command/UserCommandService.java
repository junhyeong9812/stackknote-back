package com.stacknote.back.domain.user.service.command;

import com.stacknote.back.domain.user.dto.request.PasswordChangeRequest;
import com.stacknote.back.domain.user.dto.request.UserUpdateRequest;
import com.stacknote.back.domain.user.dto.response.UserResponse;
import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.domain.user.exception.DuplicateEmailException;
import com.stacknote.back.domain.user.exception.InvalidPasswordException;
import com.stacknote.back.domain.user.exception.UserNotFoundException;
import com.stacknote.back.domain.user.repository.AccessTokenRepository;
import com.stacknote.back.domain.user.repository.RefreshTokenRepository;
import com.stacknote.back.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 관련 명령 서비스
 * 사용자 정보 수정, 삭제, 비밀번호 변경 등의 명령 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandService {

    private final UserRepository userRepository;
    private final AccessTokenRepository accessTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 정보 수정
     */
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        log.info("사용자 정보 수정 시도: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // 사용자명 중복 확인 (자신의 기존 사용자명이 아닌 경우)
        if (request.getUsername() != null &&
                !request.getUsername().equals(user.getUsername()) &&
                userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateEmailException("이미 사용 중인 사용자명입니다.");
        }

        // 사용자 정보 업데이트
        user.updateProfile(request.getUsername(), request.getProfileImageUrl());
        User updatedUser = userRepository.save(user);

        log.info("사용자 정보 수정 완료: {}", userId);
        return UserResponse.from(updatedUser);
    }

    /**
     * 비밀번호 변경
     */
    public void changePassword(Long userId, PasswordChangeRequest request) {
        log.info("비밀번호 변경 시도: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // 새 비밀번호 일치 확인
        if (!request.isNewPasswordMatched()) {
            throw new InvalidPasswordException("새 비밀번호가 일치하지 않습니다.");
        }

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("현재 비밀번호가 올바르지 않습니다.");
        }

        // 새 비밀번호가 현재 비밀번호와 같은지 확인
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new InvalidPasswordException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        // 비밀번호 변경
        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 보안상 모든 토큰 철회 (비밀번호 변경 시 재로그인 필요)
        revokeAllUserTokens(user);

        log.info("비밀번호 변경 완료: {}", userId);
    }

    /**
     * 이메일 인증 완료
     */
    public UserResponse verifyEmail(Long userId) {
        log.info("이메일 인증 완료 처리: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        user.verifyEmail();
        User updatedUser = userRepository.save(user);

        log.info("이메일 인증 완료: {}", userId);
        return UserResponse.from(updatedUser);
    }

    /**
     * 계정 비활성화
     */
    public void deactivateAccount(Long userId) {
        log.info("계정 비활성화 시도: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        user.deactivate();
        userRepository.save(user);

        // 모든 토큰 철회
        revokeAllUserTokens(user);

        log.info("계정 비활성화 완료: {}", userId);
    }

    /**
     * 계정 활성화
     */
    public UserResponse activateAccount(Long userId) {
        log.info("계정 활성화 시도: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        user.activate();
        User updatedUser = userRepository.save(user);

        log.info("계정 활성화 완료: {}", userId);
        return UserResponse.from(updatedUser);
    }

    /**
     * 계정 삭제 (소프트 삭제)
     */
    public void deleteAccount(Long userId) {
        log.info("계정 삭제 시도: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // 소프트 삭제 처리
        user.markAsDeleted();
        userRepository.save(user);

        // 모든 토큰 철회
        revokeAllUserTokens(user);

        log.info("계정 삭제 완료: {}", userId);
    }

    /**
     * 사용자의 모든 토큰 철회
     */
    private void revokeAllUserTokens(User user) {
        accessTokenRepository.revokeAllByUser(user);
        refreshTokenRepository.revokeAllByUser(user);
    }
}