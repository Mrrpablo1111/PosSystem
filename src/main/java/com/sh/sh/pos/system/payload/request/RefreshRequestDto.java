package com.sh.sh.pos.system.payload.request;

import lombok.Data;

@Data
public class RefreshRequestDto {
     private String refreshToken;
    private String deviceId;
}
