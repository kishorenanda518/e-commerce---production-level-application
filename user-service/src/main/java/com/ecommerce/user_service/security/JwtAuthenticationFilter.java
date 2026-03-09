package com.ecommerce.user_service.security;

import com.ecommerce.user_service.config.JwtProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil        jwtUtil;
    private final JwtProperties  jwtProperties;

    @Override
    protected void doFilterInternal(
            HttpServletRequest  request,
            HttpServletResponse response,
            FilterChain         filterChain) throws ServletException, IOException {

        try {
            // ── Step 1: Extract token from HttpOnly cookie ───────────
            String token = extractTokenFromCookie(request);

            // ── Step 2: Validate token ───────────────────────────────
            if (token != null && jwtUtil.validateToken(token)) {

                // ── Step 3: Extract claims ───────────────────────────
                String       userId   = jwtUtil.extractUserId(token);
                List<String> roles    = jwtUtil.extractRoles(token);

                // ── Step 4: Build authorities ────────────────────────
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                // ── Step 5: Set authentication in SecurityContext ────
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,    // principal = userId
                                null,      // credentials = null (already authenticated)
                                authorities
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT authenticated user: {} | path: {}", userId, request.getRequestURI());
            }

        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    // ── Extract JWT from HttpOnly Cookie ────────────────────────────
    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        String cookieName = jwtProperties.getCookie().getAccessTokenName();

        return Arrays.stream(cookies)
                .filter(c -> cookieName.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    // ── Skip filter for public endpoints ────────────────────────────
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/v1/auth/register")     ||
               path.startsWith("/api/v1/auth/login")        ||
               path.startsWith("/api/v1/auth/verify-email") ||
               path.startsWith("/api/v1/auth/resend-verification") ||
               path.startsWith("/api/v1/auth/forgot-password")     ||
               path.startsWith("/api/v1/auth/reset-password")      ||
               path.startsWith("/swagger-ui")               ||
               path.startsWith("/v3/api-docs")              ||
               path.startsWith("/actuator/health");
    }
}