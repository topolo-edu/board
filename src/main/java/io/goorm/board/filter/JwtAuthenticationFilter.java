package io.goorm.board.filter;

import org.springframework.beans.factory.annotation.Qualifier;
import io.goorm.board.util.JwtUtil;
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
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
                                 @Qualifier("jwtUserDetailsService") UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // /jwt/** 또는 /pages/** 경로에서만 동작 (단, /pages/auth/** 제외)
        String requestURI = request.getRequestURI();
        if (!requestURI.startsWith("/jwt/") && !requestURI.startsWith("/pages/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // /pages/auth/** 경로는 제외 (로그인 페이지)
        if (requestURI.startsWith("/pages/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Authorization 헤더 또는 쿠키에서 JWT 토큰 추출
            String jwt = getJwtFromRequest(request);
            log.debug("Authorization 헤더에서 추출한 JWT: {}", jwt != null ? "있음" : "없음");

            if (jwt == null) {
                jwt = getJwtFromCookie(request);
                log.debug("Cookie에서 추출한 JWT: {}", jwt != null ? "있음" : "없음");
            }

            if (StringUtils.hasText(jwt)) {
                log.debug("JWT 토큰 유효성 검사 시작");
                if (jwtUtil.validateToken(jwt)) {
                    // 토큰에서 이메일 추출
                    String email = jwtUtil.getEmailFromToken(jwt);
                    log.debug("JWT에서 추출한 이메일: {}", email);

                    // UserDetailsService를 통해 사용자 정보 조회
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    // Spring Security 인증 객체 생성
                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // SecurityContext에 인증 정보 설정
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("JWT 인증 성공: {}", email);
                } else {
                    log.debug("JWT 토큰 유효성 검사 실패");
                }
            } else {
                log.debug("JWT 토큰이 없음");
            }
        } catch (Exception e) {
            log.error("JWT 인증 처리 중 오류 발생: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Authorization 헤더에서 JWT 토큰 추출
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 쿠키에서 JWT 토큰 추출
     */
    private String getJwtFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}