package com.stacknote.back.global.security.configurer;

import com.stacknote.back.domain.user.service.command.AuthCommandService;
import com.stacknote.back.global.security.filter.TokenReissueFilter;
import com.stacknote.back.global.security.handler.TokenReissueFailureHandler;
import com.stacknote.back.global.security.handler.TokenReissueSuccessHandler;
import com.stacknote.back.global.utils.CookieUtil;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 토큰 갱신 필터 설정을 위한 Configurer
 * Spring Security DSL 스타일로 토큰 갱신 필터 설정을 캡슐화
 */
public class TokenReissueConfigurer extends AbstractHttpConfigurer<TokenReissueConfigurer, HttpSecurity> {

    private String reissueUrl = "/api/auth/refresh";

    /**
     * 토큰 갱신 처리 URL 설정
     */
    public TokenReissueConfigurer reissueUrl(String reissueUrl) {
        this.reissueUrl = reissueUrl;
        return this;
    }

    /**
     * HttpSecurity에 토큰 갱신 필터 추가
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {
        // ApplicationContext에서 필요한 빈들 가져오기
        AuthCommandService authCommandService = http.getSharedObject(AuthCommandService.class);
        if (authCommandService == null) {
            authCommandService = getBuilder().getSharedObject(AuthCommandService.class);
        }

        CookieUtil cookieUtil = http.getSharedObject(CookieUtil.class);
        if (cookieUtil == null) {
            cookieUtil = getBuilder().getSharedObject(CookieUtil.class);
        }

        // TokenReissueFilter 생성 및 설정
        TokenReissueFilter tokenReissueFilter = new TokenReissueFilter(
                reissueUrl,
                authCommandService,
                cookieUtil
        );

        // 성공/실패 핸들러 설정
        tokenReissueFilter.setSuccessHandler(
                new TokenReissueSuccessHandler(cookieUtil)
        );
        tokenReissueFilter.setFailureHandler(
                new TokenReissueFailureHandler(cookieUtil)
        );

        // 필터를 SecurityFilterChain에 추가
        // CustomLoginFilter 다음에 실행되도록 설정
        http.addFilterBefore(tokenReissueFilter, UsernamePasswordAuthenticationFilter.class);
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