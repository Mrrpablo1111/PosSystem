package com.sh.sh.pos.system.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {

    @NotBlank(message = "Email or phone number is required")
    private String identifier;
}
