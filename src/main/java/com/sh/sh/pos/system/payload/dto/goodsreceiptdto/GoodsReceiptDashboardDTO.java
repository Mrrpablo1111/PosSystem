package com.sh.sh.pos.system.payload.dto.goodsreceiptdto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceiptDashboardDTO {

    private Long todayReceipts;
    private Long pendingReceipts;      // DRAFT + RECEIVING
    private Long completedReceipts;
    private Long damagedItems;         // sum of damagedQuantity on completed receipts

    private List<RecentGoodsReceiptDTO>    recentReceipts;
    private List<PendingPurchaseOrderDTO>  pendingPurchaseOrders;
    private List<TopSupplierDTO>           topSuppliers;
    private List<MonthlyReceivingTrendDTO> monthlyTrend;
}
