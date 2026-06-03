package com.sh.sh.pos.system.model.session;

import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;

@Data

public class UserSession implements Serializable{
    private static final long serialVersionUID = 1L;
    private String refreshToken;
    private String deviceId;
    private String role;
    private String ip;
    private LocalDateTime loginTime;
    
}
