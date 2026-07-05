package com.sh.sh.pos.system.service.suppliersservice;


import com.sh.sh.pos.system.payload.dto.suppliersdto.SupplierLedgerDTO;

public interface SupplierLedgerService {

    SupplierLedgerDTO getLedger(Long supplierId);

}