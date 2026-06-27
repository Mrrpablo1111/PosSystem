package com.sh.sh.pos.system.payload.dto.storeMailConfigsDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor
public class StoreMailConfigDTO {
    private Long    id;
    private Long    storeId;
    private String  fromEmail;
    private String  fromName;
    private String  smtpHost;
    private Integer smtpPort;
    private Boolean active;
    // NOTE: never return appPassword in the DTO
}
