package com.sh.sh.pos.system.service.purchaseOrdersPdfService;

import com.sh.sh.pos.system.model.purchasesOrder.PurchaseOrder;

public interface PurchaseOrderPdfService {
 
    /**
     * Generates a PDF for the given PO and returns it as a byte array.
     * Used as an attachment in the supplier email.
     */
    byte[] generatePdf(PurchaseOrder order);
}
 