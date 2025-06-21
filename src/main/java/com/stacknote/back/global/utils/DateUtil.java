package com.stacknote.back.global.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * 날짜 처리 유틸리티 클래스
 * 날짜 변환, 포맷팅, 계산 등의 기능 제공
 */
@Slf4j
@Component
public class DateUtil {

    // 공통 날짜 포맷터들
    public static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter ISO_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    public static final DateTimeFormatter KOREAN_DATE = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
    public static final DateTimeFormatter KOREAN_DATETIME = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분");
    public static final DateTimeFormatter SIMPLE_DATE = DateTimeFormatter.ofPattern("MM/dd");
    public static final DateTimeFormatter SIMPLE_DATETIME = DateTimeFormatter.ofPattern("MM/dd HH:mm");
    public static final DateTimeFormatter TIME_ONLY = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 현재 시간 반환 (시스템 기본 타임존)
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * 현재 날짜 반환 (시스템 기본 타임존)
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * LocalDateTime을 한국어 형식으로 포맷팅
     * 예: "2024년 03월 15일 14시 30분"
     */
    public static String formatToKorean(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(KOREAN_DATETIME) : "";
    }

    /**
     * LocalDate를 한국어 형식으로 포맷팅
     * 예: "2024년 03월 15일"
     */
    public static String formatToKorean(LocalDate date) {
        return date != null ? date.format(KOREAN_DATE) : "";
    }

    /**
     * LocalDateTime을 ISO 형식으로 포맷팅
     * 예: "2024-03-15T14:30:00"
     */
    public static String formatToIso(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(ISO_DATETIME) : "";
    }

    /**
     * LocalDate를 ISO 형식으로 포맷팅
     * 예: "2024-03-15"
     */
    public static String formatToIso(LocalDate date) {
        return date != null ? date.format(ISO_DATE) : "";
    }

    /**
     * 상대적 시간 표현으로 변환
     * 예: "방금 전", "5분 전", "2시간 전", "3일 전"
     */
    public static String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(dateTime, now);

        long seconds = duration.getSeconds();
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (seconds < 60) {
            return "방금 전";
        } else if (minutes < 60) {
            return minutes + "분 전";
        } else if (hours < 24) {
            return hours + "시간 전";
        } else if (days < 7) {
            return days + "일 전";
        } else if (days < 30) {
            long weeks = days / 7;
            return weeks + "주 전";
        } else if (days < 365) {
            long months = days / 30;
            return months + "개월 전";
        } else {
            long years = days / 365;
            return years + "년 전";
        }
    }

    /**
     * 간단한 날짜 표현
     * 오늘이면 시간만, 이번 주면 요일, 그 외에는 날짜 표시
     */
    public static String getSimpleDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        LocalDate now = LocalDate.now();
        LocalDate targetDate = dateTime.toLocalDate();

        if (targetDate.equals(now)) {
            // 오늘이면 시간만 표시
            return dateTime.format(TIME_ONLY);
        } else if (isThisWeek(targetDate)) {
            // 이번 주면 요일과 시간 표시
            String dayOfWeek = getDayOfWeekInKorean(targetDate.getDayOfWeek());
            return dayOfWeek + " " + dateTime.format(TIME_ONLY);
        } else if (targetDate.getYear() == now.getYear()) {
            // 올해면 월/일 시:분 표시
            return dateTime.format(SIMPLE_DATETIME);
        } else {
            // 다른 해면 전체 날짜 표시
            return dateTime.format(ISO_DATE);
        }
    }

    /**
     * 지정된 날짜가 이번 주인지 확인
     */
    public static boolean isThisWeek(LocalDate date) {
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        return !date.isBefore(startOfWeek) && !date.isAfter(endOfWeek);
    }

    /**
     * 요일을 한국어로 변환
     */
    public static String getDayOfWeekInKorean(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "월요일";
            case TUESDAY -> "화요일";
            case WEDNESDAY -> "수요일";
            case THURSDAY -> "목요일";
            case FRIDAY -> "금요일";
            case SATURDAY -> "토요일";
            case SUNDAY -> "일요일";
        };
    }

    /**
     * 두 날짜 사이의 일수 계산
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * 두 시간 사이의 시간 차이 계산 (분 단위)
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * 현재 시간으로부터 지정된 분 후의 시간 계산
     */
    public static LocalDateTime afterMinutes(long minutes) {
        return LocalDateTime.now().plusMinutes(minutes);
    }

    /**
     * 현재 시간으로부터 지정된 시간 후의 시간 계산
     */
    public static LocalDateTime afterHours(long hours) {
        return LocalDateTime.now().plusHours(hours);
    }

    /**
     * 현재 날짜로부터 지정된 일 후의 날짜 계산
     */
    public static LocalDate afterDays(long days) {
        return LocalDate.now().plusDays(days);
    }

    /**
     * 현재 날짜로부터 지정된 달 후의 날짜 계산
     */
    public static LocalDate afterMonths(long months) {
        return LocalDate.now().plusMonths(months);
    }

    /**
     * 해당 날짜가 오늘인지 확인
     */
    public static boolean isToday(LocalDate date) {
        return date != null && date.equals(LocalDate.now());
    }

    /**
     * 해당 날짜가 어제인지 확인
     */
    public static boolean isYesterday(LocalDate date) {
        return date != null && date.equals(LocalDate.now().minusDays(1));
    }

    /**
     * 해당 날짜가 내일인지 확인
     */
    public static boolean isTomorrow(LocalDate date) {
        return date != null && date.equals(LocalDate.now().plusDays(1));
    }

    /**
     * 날짜를 epoch 밀리초로 변환
     */
    public static long toEpochMilli(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * epoch 밀리초를 LocalDateTime으로 변환
     */
    public static LocalDateTime fromEpochMilli(long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
    }

    /**
     * 문자열을 LocalDateTime으로 파싱 (ISO 형식)
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        try {
            return LocalDateTime.parse(dateTimeString, ISO_DATETIME);
        } catch (Exception e) {
            log.warn("Failed to parse datetime string: {}", dateTimeString);
            return null;
        }
    }

    /**
     * 문자열을 LocalDate로 파싱 (ISO 형식)
     */
    public static LocalDate parseDate(String dateString) {
        try {
            return LocalDate.parse(dateString, ISO_DATE);
        } catch (Exception e) {
            log.warn("Failed to parse date string: {}", dateString);
            return null;
        }
    }

    /**
     * 시작 시간과 종료 시간 사이의 기간을 사람이 읽기 쉬운 형태로 변환
     * 예: "2시간 30분", "1일 5시간"
     */
    public static String formatDuration(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return "";
        }

        Duration duration = Duration.between(start, end);
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        StringBuilder result = new StringBuilder();

        if (days > 0) {
            result.append(days).append("일 ");
        }
        if (hours > 0) {
            result.append(hours).append("시간 ");
        }
        if (minutes > 0) {
            result.append(minutes).append("분");
        }

        return result.toString().trim();
    }

    /**
     * 월의 시작일 반환
     */
    public static LocalDate getStartOfMonth(LocalDate date) {
        return date.withDayOfMonth(1);
    }

    /**
     * 월의 마지막일 반환
     */
    public static LocalDate getEndOfMonth(LocalDate date) {
        return date.withDayOfMonth(date.lengthOfMonth());
    }

    /**
     * 주의 시작일 반환 (월요일)
     */
    public static LocalDate getStartOfWeek(LocalDate date) {
        return date.with(DayOfWeek.MONDAY);
    }

    /**
     * 주의 마지막일 반환 (일요일)
     */
    public static LocalDate getEndOfWeek(LocalDate date) {
        return date.with(DayOfWeek.SUNDAY);
    }
}