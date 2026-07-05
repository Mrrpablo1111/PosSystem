package com.sh.sh.pos.system.repository.poemaillogrepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.poemaillogs.POEmailLog;

public interface POEmailLogRepository extends JpaRepository<POEmailLog, Long> {
 
    /** All email history for a single PO — shown in the PO detail drawer */
    List<POEmailLog> findByPurchaseOrderIdOrderBySentAtDesc(Long purchaseOrderId);
 
    /** All emails for a store's POs — for the email history page */
    List<POEmailLog> findByPurchaseOrderBranchStoreIdOrderBySentAtDesc(Long storeId);
}
 