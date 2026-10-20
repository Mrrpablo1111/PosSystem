package com.sh.sh.pos.system.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data 
public class VerifyAccountRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;

    @NotBlank (message = "Verification code is required")
    @Pattern (
        regexp = "^\\d{6}$",
        message = "OTP must contain exactly 6 digits"
    )
    private String otp;



    
}
