package com.stacknote.back.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA 관련 설정
 * - JPA Auditing 활성화 (BaseTimeEntity의 @CreatedDate, @LastModifiedDate 동작)
 * - JPA Repository 스캔 설정
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(
        basePackages = "com.stacknote.back.domain.*.repository",
        repositoryImplementationPostfix = "Impl"
)
public class JpaConfig {

}