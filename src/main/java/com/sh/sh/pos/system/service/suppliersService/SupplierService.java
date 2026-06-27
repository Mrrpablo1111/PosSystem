package com.sh.sh.pos.system.service.suppliersService;

import java.util.List;

import com.sh.sh.pos.system.payload.dto.suppliersDTO.SupplierDTO;

public interface SupplierService {

    SupplierDTO create(SupplierDTO dto);

    SupplierDTO update(Long id, SupplierDTO dto);

    SupplierDTO getById(Long id);

    List<SupplierDTO> getByStore(Long storeId);

    
    void inactive(Long id);

    void activate(Long id);
}