package com.sh.sh.pos.system.service.purchasesorderservice;

import java.util.List;

import com.sh.sh.pos.system.payload.dto.purchasesorderdto.PurchaseOrderDTO;
import com.sh.sh.pos.system.payload.request.CreatePurchaseOrderRequest;
import com.sh.sh.pos.system.payload.request.ReceiveGoodsRequestDTO;
import com.sh.sh.pos.system.payload.request.UpdateTrackingRequestDTO;

public interface PurchaseOrderService {

    List<PurchaseOrderDTO> getAllByStore(Long storeId);

    PurchaseOrderDTO getById(Long id);

    // ── Write ─────────────────────────────────────────────────────────────

    PurchaseOrderDTO create(CreatePurchaseOrderRequest req);

    PurchaseOrderDTO approve(Long id);

    PurchaseOrderDTO cancel(Long id);

    // ── Receive goods ─────────────────────────────────────────────────────

    PurchaseOrderDTO receiveGoods(ReceiveGoodsRequestDTO req);

    // ── Email ─────────────────────────────────────────────────────────────

    PurchaseOrderDTO sendToSupplier(Long id);

    // ── Tracking ──────────────────────────────────────────────────────────

    PurchaseOrderDTO updateTracking(Long id, UpdateTrackingRequestDTO req);

    List<PurchaseOrderDTO> getAll();
}
