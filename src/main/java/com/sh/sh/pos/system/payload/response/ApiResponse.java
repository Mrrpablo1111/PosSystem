package com.sh.sh.pos.system.payload.response;


import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)   // null fields are omitted from JSON
public class ApiResponse<T> {

    private boolean success = true;
    private String message;
    private T data;
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Backward-compatible with every existing `new ApiResponse("...")`
     * call in your current controllers — do not remove.
     */
    public ApiResponse(String message) {
        this.message = message;
        this.success = true;
    }

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}