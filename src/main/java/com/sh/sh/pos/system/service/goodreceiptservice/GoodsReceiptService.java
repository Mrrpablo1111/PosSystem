package com.sh.sh.pos.system.service.goodreceiptservice;

import java.util.List;

import com.sh.sh.pos.system.payload.dto.goodsreceiptdto.GoodsReceiptCreateDTO;
import com.sh.sh.pos.system.payload.dto.goodsreceiptdto.GoodsReceiptDTO;
import com.sh.sh.pos.system.payload.dto.goodsreceiptdto.GoodsReceiptDashboardDTO;
import com.sh.sh.pos.system.payload.dto.goodsreceiptdto.GoodsReceiptResponseDTO;
import com.sh.sh.pos.system.payload.dto.goodsreceiptdto.GoodsReceiptUpdateDTO;

public interface GoodsReceiptService {

    List<GoodsReceiptResponseDTO> getAllGoodsReceipts();

    List<GoodsReceiptResponseDTO> getGoodsReceiptsByPurchaseOrder(Long purchaseOrderId);

    GoodsReceiptDTO getGoodsReceiptById(Long id);

    GoodsReceiptDTO createGoodsReceipt(GoodsReceiptCreateDTO dto);

    GoodsReceiptDTO updateGoodsReceipt(Long id, GoodsReceiptUpdateDTO dto);

    void deleteGoodsReceipt(Long id);

    /** DRAFT → RECEIVING (goods physically arriving / under inspection) */
    GoodsReceiptDTO startReceiving(Long id);

    /**
     * DRAFT/RECEIVING → COMPLETED.
     * Updates PO item received/remaining quantities and PO status, creates
     * product batches, inventory transactions, and increments stock.
     */
    GoodsReceiptDTO completeGoodsReceipt(Long id);

    /** DRAFT/RECEIVING → CANCELLED (no stock or PO impact) */
    GoodsReceiptDTO cancelGoodsReceipt(Long id);

    GoodsReceiptDashboardDTO getDashboard();
}
