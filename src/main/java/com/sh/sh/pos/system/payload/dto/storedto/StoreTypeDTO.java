package com.sh.sh.pos.system.payload.dto.storedto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
@Builder 

public class StoreTypeDTO {

    private Long id;

    @NotBlank(message = "Store type code is required")
    @Size(max = 50, message = "Code Cannot exceed 50 characters")
    private String code;

    @NotBlank(message = "Store type name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private boolean active;

    
}
