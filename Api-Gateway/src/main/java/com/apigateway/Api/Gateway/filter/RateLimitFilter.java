package com.apigateway.Api.Gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    // ── Simple in-memory rate limiter ─────────────────────────
    // For production use Redis-based rate limiter
    private static final int MAX_REQUESTS_PER_MINUTE = 100;

    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long>          windowStart   = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String clientIp = getClientIp(exchange);
        String path     = exchange.getRequest().getURI().getPath();

        // skip rate limiting for health checks
        if (path.startsWith("/actuator")) {
            return chain.filter(exchange);
        }

        long now = System.currentTimeMillis();

        // reset window every 60 seconds per IP
        windowStart.compute(clientIp, (k, start) -> {
            if (start == null || (now - start) > 60_000) {
                requestCounts.put(clientIp, new AtomicInteger(0));
                return now;
            }
            return start;
        });

        int count = requestCounts
                .computeIfAbsent(clientIp, k -> new AtomicInteger(0))
                .incrementAndGet();

        if (count > MAX_REQUESTS_PER_MINUTE) {
            log.warn("Rate limit exceeded for IP: {} | count: {}", clientIp, count);
            return rateLimitResponse(exchange);
        }

        return chain.filter(exchange);
    }

    private String getClientIp(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest()
                .getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null) return xForwardedFor.split(",")[0].trim();
        var addr = exchange.getRequest().getRemoteAddress();
        return addr != null ? addr.getAddress().getHostAddress() : "unknown";
    }

    private Mono<Void> rateLimitResponse(ServerWebExchange exchange) {
        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"status\":429,\"error\":\"Too Many Requests\"," +
                "\"message\":\"Rate limit exceeded. Try again in 1 minute.\"}";

        var buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}