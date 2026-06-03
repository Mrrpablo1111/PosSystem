package com.sh.sh.pos.system.payload.request;

import lombok.Data;

@Data
public class ResetPasswordDto {
    private String token;
    private String newPassword;
}