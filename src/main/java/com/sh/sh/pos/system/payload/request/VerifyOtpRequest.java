package com.sh.sh.pos.system.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data 
public class VerifyOtpRequest {
    @NotBlank(message = "Email or phone number is required")
    private String identifier;

    @NotBlank(message = "OTP is required")
    private String otp;
}
