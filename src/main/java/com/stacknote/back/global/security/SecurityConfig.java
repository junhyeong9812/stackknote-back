package com.stacknote.back.global.security;

import com.stacknote.back.global.security.JwtAuthenticationEntryPoint;
import com.stacknote.back.global.security.JwtAuthenticationFilter;
import com.stacknote.back.global.security.configurer.CustomLoginConfigurer;
import com.stacknote.back.global.security.configurer.TokenReissueConfigurer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 설정 - Filter 기반 인증으로 확장
 * 기존 JWT 필터에 CustomLogin, TokenReissue 필터 추가
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    // 환경변수에서 허용할 origin들을 읽어옴
    @Value("${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:3001}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 기본 설정 (기존과 동일)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint(jwtAuthenticationEntryPoint))

                // 커스텀 필터 설정 - 새로 추가된 부분
                .with(new CustomLoginConfigurer(), configurer -> {
                    configurer.loginProcessingUrl("/api/auth/login");
                })
                .with(new TokenReissueConfigurer(), configurer -> {
                    configurer.reissueUrl("/api/auth/refresh");
                })

                // URL별 권한 설정 - 필터 처리 URL 고려
                .authorizeHttpRequests(auth -> auth
                        // 회원가입, 상태확인은 여전히 Controller에서 처리
                        .requestMatchers("/api/auth/register", "/api/auth/status").permitAll()

                        // 로그인, 토큰갱신은 필터에서 처리하므로 permitAll
                        .requestMatchers("/api/auth/login", "/api/auth/refresh").permitAll()

                        // 로그아웃은 여전히 Controller에서 처리 (인증 필요)
                        .requestMatchers("/api/auth/logout", "/api/auth/logout-all").authenticated()

                        // 기타 설정 (기존과 동일)
                        .requestMatchers("/api/h2-console/**").permitAll()
                        .requestMatchers("/api/swagger-ui/**", "/api/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/actuator/health").permitAll()
                        .requestMatchers("/api/files/**").authenticated()
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin()));

        // JWT 필터는 기존 위치에 유지 (다른 필터들보다 먼저 실행)
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 환경변수에서 허용할 origin들을 파싱
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
        ));

        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // 자격 증명 허용
        configuration.setAllowCredentials(true);

        // 노출할 헤더
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"
        ));

        // Preflight 요청 캐시 시간
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}