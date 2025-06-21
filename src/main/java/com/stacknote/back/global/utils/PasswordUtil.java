package com.stacknote.back.global.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 비밀번호 관련 유틸리티 클래스
 * 임시 비밀번호 생성, 비밀번호 강도 검증 등의 기능 제공
 */
@Slf4j
@Component
public class PasswordUtil {

    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*";

    private static final SecureRandom RANDOM = new SecureRandom();

    // 기본 임시 비밀번호 길이
    private static final int DEFAULT_TEMP_PASSWORD_LENGTH = 12;

    /**
     * 임시 비밀번호 생성 (기본 길이: 12자)
     * 대소문자, 숫자, 특수문자를 모두 포함하여 강력한 비밀번호 생성
     *
     * @return 생성된 임시 비밀번호
     */
    public String generateTemporaryPassword() {
        return generateTemporaryPassword(DEFAULT_TEMP_PASSWORD_LENGTH);
    }

    /**
     * 지정된 길이의 임시 비밀번호 생성
     *
     * @param length 비밀번호 길이 (최소 8자)
     * @return 생성된 임시 비밀번호
     */
    public String generateTemporaryPassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("비밀번호 길이는 최소 8자 이상이어야 합니다.");
        }

        // 각 문자 타입에서 최소 1개씩 선택
        StringBuilder password = new StringBuilder();

        // 필수 문자들 추가 (각 타입에서 최소 1개)
        password.append(getRandomChar(LOWERCASE));
        password.append(getRandomChar(UPPERCASE));
        password.append(getRandomChar(DIGITS));
        password.append(getRandomChar(SPECIAL_CHARS));

        // 나머지 길이만큼 랜덤하게 추가
        String allChars = LOWERCASE + UPPERCASE + DIGITS + SPECIAL_CHARS;
        for (int i = 4; i < length; i++) {
            password.append(getRandomChar(allChars));
        }

        // 문자 순서 섞기
        List<Character> passwordChars = password.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(passwordChars, RANDOM);

        String generatedPassword = passwordChars.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());

        log.debug("임시 비밀번호 생성 완료 (길이: {})", length);
        return generatedPassword;
    }

    /**
     * 숫자만으로 구성된 간단한 임시 PIN 생성
     *
     * @param length PIN 길이 (기본 6자리)
     * @return 생성된 숫자 PIN
     */
    public String generateNumericPin(int length) {
        if (length < 4) {
            throw new IllegalArgumentException("PIN 길이는 최소 4자리 이상이어야 합니다.");
        }

        StringBuilder pin = new StringBuilder();
        for (int i = 0; i < length; i++) {
            pin.append(RANDOM.nextInt(10));
        }

        log.debug("숫자 PIN 생성 완료 (길이: {})", length);
        return pin.toString();
    }

    /**
     * 기본 6자리 숫자 PIN 생성
     */
    public String generateNumericPin() {
        return generateNumericPin(6);
    }

    /**
     * 비밀번호 강도 검증
     *
     * @param password 검증할 비밀번호
     * @return 비밀번호 강도 (WEAK, MEDIUM, STRONG)
     */
    public PasswordStrength checkPasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            return PasswordStrength.WEAK;
        }

        int score = 0;

        // 길이 점수
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.length() >= 16) score++;

        // 문자 타입 점수
        if (password.chars().anyMatch(Character::isLowerCase)) score++;
        if (password.chars().anyMatch(Character::isUpperCase)) score++;
        if (password.chars().anyMatch(Character::isDigit)) score++;
        if (password.chars().anyMatch(c -> SPECIAL_CHARS.indexOf(c) >= 0)) score++;

        // 점수에 따른 강도 판정
        if (score >= 6) return PasswordStrength.STRONG;
        if (score >= 4) return PasswordStrength.MEDIUM;
        return PasswordStrength.WEAK;
    }

    /**
     * 비밀번호가 최소 요구사항을 만족하는지 확인
     *
     * @param password 검증할 비밀번호
     * @return 최소 요구사항 만족 여부
     */
    public boolean isValidPassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 20) {
            return false;
        }

        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(c -> SPECIAL_CHARS.indexOf(c) >= 0);

        return hasLower && hasUpper && hasDigit && hasSpecial;
    }

    /**
     * 사용자 친화적인 임시 비밀번호 생성
     * 혼동하기 쉬운 문자(0, O, l, 1 등) 제외
     *
     * @return 사용자 친화적인 임시 비밀번호
     */
    public String generateUserFriendlyPassword() {
        return generateUserFriendlyPassword(10);
    }

    /**
     * 지정된 길이의 사용자 친화적인 임시 비밀번호 생성
     *
     * @param length 비밀번호 길이
     * @return 사용자 친화적인 임시 비밀번호
     */
    public String generateUserFriendlyPassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("비밀번호 길이는 최소 8자 이상이어야 합니다.");
        }

        // 혼동하기 쉬운 문자 제외
        String friendlyLowercase = "abcdefghijkmnpqrstuvwxyz"; // l, o 제외
        String friendlyUppercase = "ABCDEFGHIJKLMNPQRSTUVWXYZ"; // O 제외
        String friendlyDigits = "23456789"; // 0, 1 제외
        String friendlySpecials = "!@#$%*"; // 혼동하기 쉬운 특수문자 제외

        StringBuilder password = new StringBuilder();

        // 필수 문자들 추가
        password.append(getRandomChar(friendlyLowercase));
        password.append(getRandomChar(friendlyUppercase));
        password.append(getRandomChar(friendlyDigits));
        password.append(getRandomChar(friendlySpecials));

        // 나머지 길이만큼 추가
        String allFriendlyChars = friendlyLowercase + friendlyUppercase + friendlyDigits + friendlySpecials;
        for (int i = 4; i < length; i++) {
            password.append(getRandomChar(allFriendlyChars));
        }

        // 문자 순서 섞기
        List<Character> passwordChars = password.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(passwordChars, RANDOM);

        String generatedPassword = passwordChars.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());

        log.debug("사용자 친화적 임시 비밀번호 생성 완료 (길이: {})", length);
        return generatedPassword;
    }

    /**
     * 문자열에서 랜덤 문자 선택
     */
    private char getRandomChar(String chars) {
        return chars.charAt(RANDOM.nextInt(chars.length()));
    }

    /**
     * 비밀번호 강도 열거형
     */
    public enum PasswordStrength {
        WEAK("약함"),
        MEDIUM("보통"),
        STRONG("강함");

        private final String description;

        PasswordStrength(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}