package com.sh.sh.pos.system.payload.request;

import lombok.Data;

@Data
public class UpdateUserDto {
    private String fullName;
    private String phoneNumber;
}
