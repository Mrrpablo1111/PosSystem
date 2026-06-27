package com.sh.sh.pos.system.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor 
@AllArgsConstructor
public class CurrencyRequestDTO {
     @NotBlank(message = "Code is required")
    @Size(max = 10, message = "Code must be at most 10 characters")
    private String code;   // e.g. "USD"
 
    @NotBlank(message = "Name is required")
    private String name;   // e.g. "US Dollar"
 
    @NotBlank(message = "Symbol is required")
    private String symbol; // e.g. "$"
 
    private Boolean active = true;
}
