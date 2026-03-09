package com.ecommerce.user_service.security;

import com.ecommerce.user_service.config.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CookieUtil {

    private final JwtProperties jwtProperties;

    // ── Set Access Token Cookie ──────────────────────────────────────
    public void setAccessTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = createCookie(
                jwtProperties.getCookie().getAccessTokenName(),
                token,
                (int) (jwtProperties.getJwt().getAccessTokenExpiryMs() / 1000)
        );
        response.addCookie(cookie);
    }

    // ── Set Refresh Token Cookie ─────────────────────────────────────
    public void setRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = createCookie(
                jwtProperties.getCookie().getRefreshTokenName(),
                token,
                (int) (jwtProperties.getJwt().getRefreshTokenExpiryMs() / 1000)
        );
        response.addCookie(cookie);
    }

    // ── Clear Access Token Cookie (logout) ───────────────────────────
    public void clearAccessTokenCookie(HttpServletResponse response) {
        Cookie cookie = createCookie(
                jwtProperties.getCookie().getAccessTokenName(),
                "",
                0   // maxAge=0 → browser deletes it immediately
        );
        response.addCookie(cookie);
    }

    // ── Clear Refresh Token Cookie (logout) ──────────────────────────
    public void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = createCookie(
                jwtProperties.getCookie().getRefreshTokenName(),
                "",
                0
        );
        response.addCookie(cookie);
    }

    // ── Get cookie value from request ────────────────────────────────
    public Optional<String> getCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) return Optional.empty();

        return Arrays.stream(request.getCookies())
                .filter(c -> cookieName.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    // ── Build HttpOnly cookie ────────────────────────────────────────
    private Cookie createCookie(String name, String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);   // JS cannot read — XSS safe
        cookie.setSecure(jwtProperties.getCookie().isSecure()); // HTTPS only in prod
        cookie.setPath(jwtProperties.getCookie().getPath());
        cookie.setMaxAge(maxAgeSeconds);
        return cookie;
    }
}