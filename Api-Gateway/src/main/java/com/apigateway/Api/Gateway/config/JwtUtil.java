package com.apigateway.Api.Gateway.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

@Slf4j
@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    // ── Get signing key ───────────────────────────────────────
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(
                jwtSecret.getBytes(StandardCharsets.UTF_8)
        );
    }

    // ── Validate token ────────────────────────────────────────
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("JWT invalid: {}", e.getMessage());
        }
        return false;
    }

    // ── Extract all claims ────────────────────────────────────
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ── Extract user ID ───────────────────────────────────────
    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ── Extract username ──────────────────────────────────────
    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("username", String.class);
    }

    // ── Extract roles ─────────────────────────────────────────
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        Object roles = claims.get("roles");
        if (roles instanceof List<?>) {
            return (List<String>) roles;
        }
        return List.of();
    }

    // ── Check if token has required role ─────────────────────
    public boolean hasRole(String token, String requiredRole) {
        return extractRoles(token).contains(requiredRole);
    }
}