package com.apigateway.Api.Gateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.time.Instant;

@Slf4j
@Order(-2)
@Configuration
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        HttpStatus status;
        String message;

        if (ex instanceof NotFoundException) {
            status  = HttpStatus.SERVICE_UNAVAILABLE;
            message = "Service is currently unavailable. Please try again later.";
            log.error("Service not found: {}", ex.getMessage());

        } else if (ex instanceof ConnectException) {
            status  = HttpStatus.SERVICE_UNAVAILABLE;
            message = "Cannot connect to service. Please try again later.";
            log.error("Connection refused: {}", ex.getMessage());

        } else if (ex instanceof ResponseStatusException rse) {
            status  = (HttpStatus) rse.getStatusCode();
            message = rse.getReason() != null ? rse.getReason() : ex.getMessage();

        } else if (ex instanceof java.util.concurrent.TimeoutException) {
            status  = HttpStatus.GATEWAY_TIMEOUT;
            message = "Request timed out. Please try again.";
            log.error("Gateway timeout: {}", ex.getMessage());

        } else {
            status  = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "An unexpected error occurred.";
            log.error("Unhandled gateway error: {}", ex.getMessage(), ex);
        }

        String path = exchange.getRequest().getURI().getPath();

        String body = String.format(
                "{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"," +
                "\"path\":\"%s\",\"timestamp\":\"%s\"}",
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                Instant.now()
        );

        var response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        var buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
}