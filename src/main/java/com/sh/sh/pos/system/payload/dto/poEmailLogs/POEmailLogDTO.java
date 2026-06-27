package com.sh.sh.pos.system.payload.dto.poEmailLogs;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class POEmailLogDTO {
    private Long          id;
    private Long          purchaseOrderId;
    private String        poNumber;
    private String        emailType;
    private String        sentTo;
    private String        subject;
    private String        status;
    private String        errorMessage;
    private LocalDateTime sentAt;
}
 
