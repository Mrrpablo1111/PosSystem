package com.sh.sh.pos.system.payload.dto.suppliersDTO;

import java.math.BigDecimal;

import com.sh.sh.pos.system.domain.SupplierStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDTO {

    private Long id;

    private String code;

    private String name;

    private String phone;

    private String email;

    private String address;

    private String taxNumber;
    
    private String country;

    private String contactPerson;

    private Integer paymentTermDays;

    private BigDecimal creditLimit;

    private SupplierStatus status;

    private Long storeId;
}