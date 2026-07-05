package com.sh.sh.pos.system.payload.response;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sh.sh.pos.system.domain.InternalCodeEnum;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseBody<T> {
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    private boolean success;
    private String message;

    @JsonIgnore
    private InternalCodeEnum internalCode;
    private String timestamp;
    private final T data;

    public ApiResponseBody(boolean success, String message, InternalCodeEnum internalCode, T data) {
        this.success = success;
        this.message = message;
        this.internalCode = internalCode;
        this.timestamp = currentTimestamp();
        this.data = data;
    }

    public ApiResponseBody(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.timestamp = currentTimestamp();
        this.data = data;
    }

    private static String currentTimestamp() {
        return ZonedDateTime.now(ZoneOffset.UTC).format(TIMESTAMP_FORMAT);
    }
}
