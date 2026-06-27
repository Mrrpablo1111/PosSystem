package com.sh.sh.pos.system.service.suppliersService;


import com.sh.sh.pos.system.payload.dto.suppliersDTO.SupplierLedgerDTO;

public interface SupplierLedgerService {

    SupplierLedgerDTO getLedger(Long supplierId);

}