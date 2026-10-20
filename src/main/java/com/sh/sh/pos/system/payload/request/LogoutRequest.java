package com.sh.sh.pos.system.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 
public class LogoutRequest {
    private String email;
    private String deviceId;
}
