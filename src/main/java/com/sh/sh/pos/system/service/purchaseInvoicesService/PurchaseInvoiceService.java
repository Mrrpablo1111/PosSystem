package com.sh.sh.pos.system.service.purchaseInvoicesService;

import java.util.List;


import com.sh.sh.pos.system.payload.dto.purchaseInvoicesDTO.PurchaseInvoiceDTO;

public interface PurchaseInvoiceService {

    /** Creates a draft purchase invoice from the given items. */
    PurchaseInvoiceDTO create(PurchaseInvoiceDTO dto);

    /** Posts a draft invoice: creates stock batches and records inbound stock movements. */
    void post(Long invoiceId);

    /** Returns a single invoice by id. */
    PurchaseInvoiceDTO getById(Long id);

    /** Returns all purchase invoices. */
    List<PurchaseInvoiceDTO> getAll();
}