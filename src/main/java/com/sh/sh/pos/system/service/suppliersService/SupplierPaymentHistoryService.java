package com.sh.sh.pos.system.service.suppliersService;


import com.sh.sh.pos.system.payload.dto.suppliersDTO.SupplierPaymentHistoryDTO;

import java.util.List;


public interface SupplierPaymentHistoryService {

    List<SupplierPaymentHistoryDTO> getBySupplier(Long supplierId);

    List<SupplierPaymentHistoryDTO> getByInvoice(Long invoiceId);
}