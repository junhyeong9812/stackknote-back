package com.stacknote.back.global.security;

import com.stacknote.back.domain.user.entity.AccessToken;
import com.stacknote.back.domain.user.repository.AccessTokenRepository;
import com.stacknote.back.global.utils.CookieUtil;
import com.stacknote.back.global.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * JWT 인증 필터
 * 모든 HTTP 요청에서 JWT 토큰을 검증하고 Spring Security Context에 인증 정보 설정
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final UserDetailsService userDetailsService;
    private final AccessTokenRepository accessTokenRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 1. 쿠키에서 액세스 토큰 추출
            Optional<String> tokenOptional = cookieUtil.getAccessToken(request);

            if (tokenOptional.isEmpty()) {
                log.debug("No access token found in cookies");
                filterChain.doFilter(request, response);
                return;
            }

            String token = tokenOptional.get();
            log.debug("Access token found in request");

            // 2. JWT 토큰 기본 검증 (서명, 형식, 만료시간)
            if (!jwtUtil.validateToken(token)) {
                log.debug("Invalid JWT token");
                cookieUtil.deleteAccessTokenCookie(response); // 유효하지 않은 토큰 삭제
                filterChain.doFilter(request, response);
                return;
            }

            // 3. 토큰 타입 확인
            String tokenType = jwtUtil.getTokenType(token);
            if (!"access".equals(tokenType)) {
                log.debug("Invalid token type: {}", tokenType);
                cookieUtil.deleteAccessTokenCookie(response);
                filterChain.doFilter(request, response);
                return;
            }

            // 4. 데이터베이스에서 토큰 유효성 검증 (철회 여부, 실제 존재 여부)
            Optional<AccessToken> accessTokenOptional = accessTokenRepository
                    .findValidTokenByToken(token, LocalDateTime.now());

            if (accessTokenOptional.isEmpty()) {
                log.debug("Token not found in database or expired");
                cookieUtil.deleteAccessTokenCookie(response);
                filterChain.doFilter(request, response);
                return;
            }

            // 5. 사용자 정보 로드
            String userEmail = jwtUtil.getEmailFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            // 6. 사용자 상태 검증
            if (!userDetails.isEnabled() || !userDetails.isAccountNonLocked()) {
                log.debug("User account is disabled or locked: {}", userEmail);
                cookieUtil.deleteAccessTokenCookie(response);
                filterChain.doFilter(request, response);
                return;
            }

            // 7. Spring Security Context에 인증 정보 설정
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("JWT authentication successful for user: {}", userEmail);

        } catch (Exception e) {
            log.error("JWT authentication error: {}", e.getMessage(), e);
            // 인증 실패 시 쿠키 삭제
            cookieUtil.deleteAccessTokenCookie(response);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // 인증이 필요없는 경로들
        return path.startsWith("/api/auth/") ||
                path.startsWith("/h2-console/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs/") ||
                path.equals("/actuator/health");
    }
}