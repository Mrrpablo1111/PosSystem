package com.sh.sh.pos.system.payload.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailDTO implements Serializable{
    private String to;
    private String subject;
    private String body;

    private Long storeId;

    private Long purchaseOrderId;


    public EmailDTO(String to, String subject, String body) {
        this.to      = to;
        this.subject = subject;
        this.body    = body;
        this.storeId         = null;
        this.purchaseOrderId = null;
    }
}
