package com.ecommerce.product_service.security;

import com.ecommerce.product_service.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    // ── VALIDATE TOKEN ────────────────────────────────────────────────
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    // ── EXTRACT USER ID ───────────────────────────────────────────────
    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ── EXTRACT ROLES ─────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return (List<String>) extractAllClaims(token).get("roles");
    }

    // ── EXTRACT USERNAME ──────────────────────────────────────────────
    public String extractUsername(String token) {
        return (String) extractAllClaims(token).get("username");
    }

    // ── EXTRACT EXPIRATION ────────────────────────────────────────────
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    // ── EXTRACT TOKEN ID ──────────────────────────────────────────────
    public String extractTokenId(String token) {
        return extractAllClaims(token).getId();
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
                jwtProperties.getJwt().getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}