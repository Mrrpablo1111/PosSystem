package com.sh.sh.pos.system.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginDto {

    @NotBlank(message = "Email or phone number is required")
    private String identifier;

    @NotBlank
    @NotNull
    private String password;

    private String deviceId;
}