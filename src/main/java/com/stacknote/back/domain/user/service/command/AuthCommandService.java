package com.stacknote.back.domain.user.service.command;

import com.stacknote.back.domain.user.dto.request.UserLoginRequest;
import com.stacknote.back.domain.user.dto.request.UserRegisterRequest;
import com.stacknote.back.domain.user.dto.response.AuthTokenResponse;
import com.stacknote.back.domain.user.dto.response.UserResponse;
import com.stacknote.back.domain.user.entity.AccessToken;
import com.stacknote.back.domain.user.entity.RefreshToken;
import com.stacknote.back.domain.user.entity.User;
import com.stacknote.back.domain.user.exception.DuplicateEmailException;
import com.stacknote.back.domain.user.exception.InvalidPasswordException;
import com.stacknote.back.domain.user.exception.UserNotFoundException;
import com.stacknote.back.domain.user.repository.AccessTokenRepository;
import com.stacknote.back.domain.user.repository.RefreshTokenRepository;
import com.stacknote.back.domain.user.repository.UserRepository;
import com.stacknote.back.global.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 인증 관련 명령 서비스
 * 회원가입, 로그인, 로그아웃, 토큰 갱신 등의 명령 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthCommandService {

    private final UserRepository userRepository;
    private final AccessTokenRepository accessTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 회원가입
     */
    public UserResponse register(UserRegisterRequest request) {
        log.info("사용자 회원가입 시도: {}", request.getEmail());

        // 비밀번호 일치 확인
        if (!request.isPasswordMatched()) {
            throw new InvalidPasswordException("비밀번호가 일치하지 않습니다.");
        }

        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다.");
        }

        // 사용자명 중복 확인
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateEmailException("이미 사용 중인 사용자명입니다.");
        }

        // 사용자 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .username(request.getUsername())
                .role(User.Role.USER)
                .isEmailVerified(false)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("사용자 회원가입 완료: {}", savedUser.getEmail());

        return UserResponse.from(savedUser);
    }

    /**
     * 로그인
     */
    public AuthTokenResponse login(UserLoginRequest request, String userAgent, String ipAddress) {
        log.info("사용자 로그인 시도: {}", request.getEmail());

        // 사용자 조회 및 검증
        User user = userRepository.findActiveUserByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("이메일 또는 비밀번호가 올바르지 않습니다."));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // 기존 토큰들 철회 (보안상 한 사용자당 하나의 세션만 유지)
        revokeAllUserTokens(user);

        // 새 토큰 생성 및 저장
        String accessTokenValue = jwtUtil.generateAccessToken(user.getEmail(), user.getId());
        String refreshTokenValue = jwtUtil.generateRefreshToken(user.getEmail(), user.getId());

        AccessToken accessToken = createAndSaveAccessToken(accessTokenValue, user, userAgent, ipAddress);
        RefreshToken refreshToken = createAndSaveRefreshToken(refreshTokenValue, user, userAgent, ipAddress);

        log.info("사용자 로그인 완료: {}", user.getEmail());

        return AuthTokenResponse.of(
                accessToken.getExpiresAt(),
                refreshToken.getExpiresAt(),
                UserResponse.from(user)
        );
    }

    /**
     * 리프레시 토큰으로 액세스 토큰 갱신
     */
    public AuthTokenResponse refreshToken(String refreshTokenValue, String userAgent, String ipAddress) {
        log.info("토큰 갱신 시도");

        // 리프레시 토큰 검증
        RefreshToken refreshToken = refreshTokenRepository
                .findValidTokenByToken(refreshTokenValue, LocalDateTime.now())
                .orElseThrow(() -> new InvalidPasswordException("유효하지 않은 리프레시 토큰입니다."));

        User user = refreshToken.getUser();

        // 기존 액세스 토큰 철회
        accessTokenRepository.revokeAllByUser(user);

        // 새 액세스 토큰 생성
        String newAccessTokenValue = jwtUtil.generateAccessToken(user.getEmail(), user.getId());
        AccessToken newAccessToken = createAndSaveAccessToken(newAccessTokenValue, user, userAgent, ipAddress);

        log.info("토큰 갱신 완료: {}", user.getEmail());

        return AuthTokenResponse.of(
                newAccessToken.getExpiresAt(),
                refreshToken.getExpiresAt(),
                UserResponse.from(user)
        );
    }

    /**
     * 로그아웃
     */
    public void logout(String accessTokenValue) {
        log.info("사용자 로그아웃 시도");

        // 액세스 토큰으로 사용자 찾기
        AccessToken accessToken = accessTokenRepository
                .findValidTokenByToken(accessTokenValue, LocalDateTime.now())
                .orElseThrow(() -> new InvalidPasswordException("유효하지 않은 토큰입니다."));

        User user = accessToken.getUser();

        // 모든 토큰 철회
        revokeAllUserTokens(user);

        log.info("사용자 로그아웃 완료: {}", user.getEmail());
    }

    /**
     * 모든 디바이스에서 로그아웃 (강제 로그아웃)
     */
    public void logoutFromAllDevices(Long userId) {
        log.info("사용자 모든 디바이스 로그아웃: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        revokeAllUserTokens(user);

        log.info("모든 디바이스 로그아웃 완료: {}", user.getEmail());
    }

    /**
     * 사용자의 모든 토큰 철회
     */
    private void revokeAllUserTokens(User user) {
        accessTokenRepository.revokeAllByUser(user);
        refreshTokenRepository.revokeAllByUser(user);
    }

    /**
     * 액세스 토큰 생성 및 저장
     */
    private AccessToken createAndSaveAccessToken(String tokenValue, User user, String userAgent, String ipAddress) {
        AccessToken accessToken = AccessToken.builder()
                .token(tokenValue)
                .user(user)
                .expiresAt(jwtUtil.getAccessTokenExpiryTime())
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .build();

        return accessTokenRepository.save(accessToken);
    }

    /**
     * 리프레시 토큰 생성 및 저장
     */
    private RefreshToken createAndSaveRefreshToken(String tokenValue, User user, String userAgent, String ipAddress) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiresAt(jwtUtil.getRefreshTokenExpiryTime())
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }
}