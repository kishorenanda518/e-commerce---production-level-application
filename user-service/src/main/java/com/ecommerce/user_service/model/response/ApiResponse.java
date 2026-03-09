package com.ecommerce.user_service.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // hides null fields from JSON output
public class ApiResponse<T> {

    private String status;   // "SUCCESS" or "ERROR"
    private String message;
    private T data;

    @Builder.Default
    private Instant timestamp = Instant.now();

    // ── Static factory helpers ───────────────────────────────────────

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status("SUCCESS")
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return success("Operation successful", data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .status("ERROR")
                .message(message)
                .build();
    }
}