package com.sh.sh.pos.system.service.suppliersservice;


import com.sh.sh.pos.system.payload.dto.suppliersdto.SupplierPaymentHistoryDTO;

import java.util.List;


public interface SupplierPaymentHistoryService {

    List<SupplierPaymentHistoryDTO> getBySupplier(Long supplierId);

    List<SupplierPaymentHistoryDTO> getByInvoice(Long invoiceId);
}