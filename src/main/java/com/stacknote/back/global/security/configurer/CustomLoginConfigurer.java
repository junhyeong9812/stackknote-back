package com.stacknote.back.global.security.configurer;

import com.stacknote.back.domain.user.service.command.AuthCommandService;
import com.stacknote.back.global.security.filter.CustomLoginFilter;
import com.stacknote.back.global.security.handler.CustomAuthenticationFailureHandler;
import com.stacknote.back.global.security.handler.CustomAuthenticationSuccessHandler;
import com.stacknote.back.global.utils.CookieUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * 커스텀 로그인 필터 설정을 위한 Configurer
 * Spring Security DSL 스타일로 필터 설정을 캡슐화
 */
public class CustomLoginConfigurer extends AbstractHttpConfigurer<CustomLoginConfigurer, HttpSecurity> {

    private String loginProcessingUrl = "/auth/login";

    /**
     * 로그인 처리 URL 설정
     */
    public CustomLoginConfigurer loginProcessingUrl(String loginProcessingUrl) {
        this.loginProcessingUrl = loginProcessingUrl;
        return this;
    }

    /**
     * HttpSecurity에 커스텀 로그인 필터 추가
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {
        // AuthenticationManager 가져오기
        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);

        // ApplicationContext를 통해 빈 가져오기
        org.springframework.context.ApplicationContext context =
                http.getSharedObject(org.springframework.context.ApplicationContext.class);

        AuthCommandService authCommandService = context.getBean(AuthCommandService.class);
        CookieUtil cookieUtil = context.getBean(CookieUtil.class);
        ObjectMapper objectMapper = context.getBean(ObjectMapper.class); // ObjectMapper 추가

        // CustomLoginFilter 생성 및 설정
        CustomLoginFilter customLoginFilter = new CustomLoginFilter(
                authCommandService,
                cookieUtil,
                objectMapper
        );

        // 필터 기본 설정
        customLoginFilter.setFilterProcessesUrl(loginProcessingUrl);
        customLoginFilter.setAuthenticationManager(authenticationManager);

        // 성공/실패 핸들러 설정 - ObjectMapper 전달
        customLoginFilter.setAuthenticationSuccessHandler(
                new CustomAuthenticationSuccessHandler(cookieUtil, objectMapper) // objectMapper 추가
        );
        customLoginFilter.setAuthenticationFailureHandler(
                new CustomAuthenticationFailureHandler(objectMapper) // objectMapper 추가
        );

        // POST 요청만 처리하도록 설정
        customLoginFilter.setPostOnly(true);

        // 필터를 SecurityFilterChain에 추가
        http.addFilterBefore(customLoginFilter, UsernamePasswordAuthenticationFilter.class);
    }

    /**
     * 빈을 직접 가져오는 헬퍼 메소드 (필요시 사용)
     */
    @SuppressWarnings("unchecked")
    private <T> T getBean(HttpSecurity http, Class<T> beanType) {
        return http.getSharedObject(org.springframework.context.ApplicationContext.class)
                .getBean(beanType);
    }
}