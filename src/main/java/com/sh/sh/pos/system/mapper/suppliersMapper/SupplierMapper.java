package com.sh.sh.pos.system.mapper.suppliersMapper;

import com.sh.sh.pos.system.model.suppliers.Supplier;
import com.sh.sh.pos.system.payload.dto.suppliersDTO.SupplierDTO;

public class SupplierMapper {

    public static SupplierDTO toDTO(Supplier s) {

        SupplierDTO dto = new SupplierDTO();

        dto.setId(s.getId());
        dto.setCode(s.getCode());
        dto.setName(s.getName());
        dto.setPhone(s.getPhone());
        dto.setEmail(s.getEmail());
        dto.setAddress(s.getAddress());
        dto.setTaxNumber(s.getTaxNumber());
        dto.setCountry(s.getCountry());
        dto.setContactPerson(s.getContactPerson());
        dto.setPaymentTermDays(s.getPaymentTermDays());
        dto.setCreditLimit(s.getCreditLimit());
        dto.setStatus(s.getStatus());

        if (s.getStore() != null) {
            dto.setStoreId(s.getStore().getId());
        }

        return dto;
    }
}