package com.sh.sh.pos.system.payload.dto;

import java.time.LocalDateTime;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {

    private Long id;

    private String fullName;

    private String email;

    private String phone;

    private Long priceGroupId;

    private String priceGroupName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}