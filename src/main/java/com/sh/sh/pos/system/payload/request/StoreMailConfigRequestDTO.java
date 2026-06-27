package com.sh.sh.pos.system.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor
public class StoreMailConfigRequestDTO {
 
    @NotNull
    private Long storeId;
 
    @NotBlank @Email
    private String fromEmail;
 
    private String fromName;
 
    /**
     * Gmail App Password — 16 characters.
     * Get from: Google Account → Security → 2-Step → App Passwords
     */
    @NotBlank
    private String appPassword;
 
    @Builder.Default
    private String  smtpHost = "smtp.gmail.com";
 
    @Builder.Default
    private Integer smtpPort = 587;
}
 
