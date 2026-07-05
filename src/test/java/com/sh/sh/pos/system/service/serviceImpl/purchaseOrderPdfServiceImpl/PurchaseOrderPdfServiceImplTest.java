package com.sh.sh.pos.system.service.serviceImpl.purchaseOrderPdfServiceImpl;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sh.sh.pos.system.domain.PurchaseOrderStatus;
import com.sh.sh.pos.system.model.branch.Branch;
import com.sh.sh.pos.system.model.products.Product;
import com.sh.sh.pos.system.model.purchasesorder.PurchaseOrder;
import com.sh.sh.pos.system.model.purchasesorderitem.PurchaseOrderItem;
import com.sh.sh.pos.system.model.store.Store;
import com.sh.sh.pos.system.model.suppliers.Supplier;
import com.sh.sh.pos.system.service.serviceimpl.purchaseorderspdfserviceimpl.PurchaseOrderPdfServiceImpl;

class PurchaseOrderPdfServiceImplTest {

    private final PurchaseOrderPdfServiceImpl pdfService = new PurchaseOrderPdfServiceImpl();

    @Test
    void generatePdf_pendingOrder_producesValidPdf() throws Exception {
        PurchaseOrder order = samplePo(PurchaseOrderStatus.PENDING);

        byte[] bytes = pdfService.generatePdf(order);

        assertNotNull(bytes);
        assertTrue(bytes.length > 1000, "PDF should have real content");
        // PDF magic number: %PDF
        assertTrue(bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F');

        // Write to target/ so you can open and inspect it
        Path outFile = Path.of("target", "test-po-pending.pdf");
        Files.write(outFile, bytes);
        System.out.println("PDF written to: " + outFile.toAbsolutePath());
    }

    @Test
    void generatePdf_receivedOrder_producesStampedPdf() throws Exception {
        PurchaseOrder order = samplePo(PurchaseOrderStatus.RECEIVED);
        order.setReceivedDate(LocalDate.now());

        byte[] bytes = pdfService.generatePdf(order);

        assertNotNull(bytes);
        assertTrue(bytes.length > 1000);

        Path outFile = Path.of("target", "test-po-received.pdf");
        Files.write(outFile, bytes);
        System.out.println("PDF written to: " + outFile.toAbsolutePath());
    }

    @Test
    void generatePdf_minimalOrder_handlesNullsGracefully() {
        // No supplier, no branch, no items' product details — the null-guard test
        PurchaseOrder order = new PurchaseOrder();
        order.setPoNumber("PO-MINIMAL");
        order.setOrderDate(LocalDate.now());
        order.setStatus(PurchaseOrderStatus.PENDING);
        order.setSubtotal(BigDecimal.ZERO);
        order.setTotalAmount(BigDecimal.ZERO);
        order.setItems(List.of());

        byte[] bytes = pdfService.generatePdf(order);

        assertNotNull(bytes);
        assertTrue(bytes.length > 500);
    }

    // ── Test data builder ─────────────────────────────────────────────────

    private PurchaseOrder samplePo(PurchaseOrderStatus status) {
        Store store = new Store();
        store.setBrand("Test Mart");

        Branch branch = new Branch();
        branch.setName("Main Branch");
        branch.setStore(store);

        Supplier supplier = new Supplier();
        supplier.setName("ACME Supplies Co.");
        supplier.setContactPerson("Dara Sok");
        supplier.setAddress("Street 271, Phnom Penh");
        supplier.setPhone("012 345 678");

        Product product = new Product();
        product.setName("Coca-Cola 330ml");
        product.setSku("CC-330");

        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setProduct(product);
        item.setRequestedQuantity(100);
        item.setReceivedQuantity(status == PurchaseOrderStatus.RECEIVED ? 100 : 0);
        item.setRemainingQuantity(status == PurchaseOrderStatus.RECEIVED ? 0 : 100);
        item.setUnitCost(new BigDecimal("0.45"));
        item.setTotalCost(new BigDecimal("45.00"));

        PurchaseOrder order = new PurchaseOrder();
        order.setPoNumber("PO-2026-0001");
        order.setOrderDate(LocalDate.now());
        order.setExpectedDelivery(LocalDate.now().plusDays(7));
        order.setStatus(status);
        order.setSupplier(supplier);
        order.setBranch(branch);
        order.setItems(List.of(item));
        order.setSubtotal(new BigDecimal("45.00"));
        order.setTotalAmount(new BigDecimal("45.00"));
        order.setNotes("Deliver to back entrance.");
        return order;
    }
}