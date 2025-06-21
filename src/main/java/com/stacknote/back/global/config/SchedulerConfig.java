package com.stacknote.back.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 스케줄링 활성화 설정
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
    // 스케줄링을 활성화하기 위한 설정 클래스
    // @EnableScheduling 애노테이션으로 스케줄링 기능을 활성화
}