package com.apigateway.Api.Gateway.filter;

import com.apigateway.Api.Gateway.config.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class JwtAuthFilter extends
        AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    // ── Config — set per route in application.yml ─────────────
    public static class Config {
        private boolean requireAuth = true;
        private String  requiredRole = null;   // null = any role

        public boolean isRequireAuth()           { return requireAuth;  }
        public void    setRequireAuth(boolean v) { this.requireAuth = v; }
        public String  getRequiredRole()           { return requiredRole;  }
        public void    setRequiredRole(String v)   { this.requiredRole = v; }
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            String method = request.getMethod().name();

            log.debug("Gateway filter → {} {}", method, path);

            // ── Skip auth for OPTIONS (CORS preflight) ────────
            if ("OPTIONS".equals(method)) {
                return chain.filter(exchange);
            }

            // ── Skip auth if route is public ──────────────────
            if (!config.isRequireAuth()) {
                return chain.filter(exchange);
            }

            // ── Extract token from header or cookie ───────────
            String token = extractToken(request);

            if (token == null) {
                log.warn("No token found for protected route: {}", path);
                return unauthorizedResponse(exchange, "Authentication required");
            }

            // ── Validate token ────────────────────────────────
            if (!jwtUtil.validateToken(token)) {
                log.warn("Invalid token for route: {}", path);
                return unauthorizedResponse(exchange, "Invalid or expired token");
            }

            // ── Check role if required ────────────────────────
            if (config.getRequiredRole() != null) {
                if (!jwtUtil.hasRole(token, config.getRequiredRole())) {
                    log.warn("Insufficient role for route: {} | required: {}",
                            path, config.getRequiredRole());
                    return forbiddenResponse(exchange,
                            "Access denied: requires " + config.getRequiredRole());
                }
            }

            // ── Add user info headers for downstream services ─
            String userId   = jwtUtil.extractUserId(token);
            String username = jwtUtil.extractUsername(token);
            List<String> roles = jwtUtil.extractRoles(token);

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id",    userId   != null ? userId   : "")
                    .header("X-Username",   username != null ? username : "")
                    .header("X-User-Roles", String.join(",", roles))
                    .build();

            log.debug("JWT validated → userId={} | roles={}", userId, roles);

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        };
    }

    // ── Extract token from Authorization header or cookie ─────
    private String extractToken(ServerHttpRequest request) {
        // 1. Try Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 2. Try access_token cookie
        List<String> cookieValues = request.getHeaders().get(HttpHeaders.COOKIE);
        if (cookieValues != null) {
            for (String cookie : cookieValues) {
                for (String part : cookie.split(";")) {
                    String trimmed = part.trim();
                    if (trimmed.startsWith("access_token=")) {
                        return trimmed.substring("access_token=".length());
                    }
                }
            }
        }

        return null;
    }

    // ── 401 Unauthorized response ─────────────────────────────
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        return errorResponse(exchange, HttpStatus.UNAUTHORIZED, message);
    }

    // ── 403 Forbidden response ────────────────────────────────
    private Mono<Void> forbiddenResponse(ServerWebExchange exchange, String message) {
        return errorResponse(exchange, HttpStatus.FORBIDDEN, message);
    }

    // ── Generic error response ────────────────────────────────
    private Mono<Void> errorResponse(ServerWebExchange exchange,
                                      HttpStatus status,
                                      String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format(
                "{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}",
                status.value(), status.getReasonPhrase(), message
        );

        byte[] bytes = body.getBytes();
        var buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}