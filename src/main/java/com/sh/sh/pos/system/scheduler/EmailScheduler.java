package com.sh.sh.pos.system.scheduler;



import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sh.sh.pos.system.domain.PurchaseOrderStatus;
import com.sh.sh.pos.system.model.Store;
import com.sh.sh.pos.system.model.products.ProductBatch;
import com.sh.sh.pos.system.model.purchasesOrder.PurchaseOrder;
import com.sh.sh.pos.system.repository.ProductBatchRepository;
import com.sh.sh.pos.system.repository.StoreRepository;
import com.sh.sh.pos.system.repository.purchasesOrderRepository.PurchaseOrderRepository;
import com.sh.sh.pos.system.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailScheduler {

    private final EmailService            emailService;
    private final PurchaseOrderRepository poRepository;
    private final ProductBatchRepository  batchRepository;
    private final StoreRepository         storeRepository;

    /** Minimum remaining quantity before low-stock alert fires */
    private static final int LOW_STOCK_THRESHOLD = 10;

    /** Days before expiry to start alerting */
    private static final int EXPIRY_DAYS_WARNING = 30;

    // ── Overdue PO check — runs every day at 08:00 ────────────────────────

    @Scheduled(cron = "0 0 8 * * *")
    public void checkOverduePurchaseOrders() {
        log.info("⏰ Running overdue PO check...");

        List<PurchaseOrder> overdue = poRepository.findOverdue(LocalDate.now());

        overdue.forEach(po -> {
            // Mark as OVERDUE
            po.setStatus(PurchaseOrderStatus.OVERDUE);
            poRepository.save(po);

            // Notify store manager via store contact email
            String managerEmail = null;
            try {
                managerEmail = po.getBranch().getStore().getContact().getEmail();
            } catch (Exception ignored) {}

            emailService.sendOverduePOAlert(po, managerEmail);
        });

        log.info("⏰ Overdue PO check done — {} POs flagged", overdue.size());
    }

    // ── Low stock check — runs every day at 07:00 ─────────────────────────

    @Scheduled(cron = "0 0 7 * * *")
    public void checkLowStock() {
        log.info("⏰ Running low stock check...");

        List<Store> stores = storeRepository.findAll();

        stores.forEach(store -> {
            String managerEmail = store.getContact() != null
                    ? store.getContact().getEmail() : null;

            if (managerEmail == null || managerEmail.isBlank()) return;

            List<ProductBatch> lowBatches = batchRepository
                    .findByBranchStoreIdAndRemainingQuantityLessThanEqual(
                            store.getId(), LOW_STOCK_THRESHOLD);

            if (!lowBatches.isEmpty()) {
                emailService.sendLowStockAlert(store, lowBatches, managerEmail);
                log.info("⏰ Low stock alert sent → {} ({} items)", managerEmail, lowBatches.size());
            }
        });
    }

    // ── Expiry check — runs every day at 07:30 ────────────────────────────

    @Scheduled(cron = "0 30 7 * * *")
    public void checkExpiringBatches() {
        log.info("⏰ Running expiry check...");

        LocalDate warningDate = LocalDate.now().plusDays(EXPIRY_DAYS_WARNING);

        List<Store> stores = storeRepository.findAll();

        stores.forEach(store -> {
            String managerEmail = store.getContact() != null
                    ? store.getContact().getEmail() : null;

            if (managerEmail == null || managerEmail.isBlank()) return;

            List<ProductBatch> expiring = batchRepository
                    .findByBranchStoreIdAndExpiryDateBefore(store.getId(), warningDate)
                    .stream()
                    .filter(b -> b.getRemainingQuantity() > 0)
                    .collect(Collectors.toList());

            if (!expiring.isEmpty()) {
                emailService.sendExpiryAlert(store, expiring, managerEmail);
                log.info("⏰ Expiry alert sent → {} ({} batches)", managerEmail, expiring.size());
            }
        });
    }

    // ── Daily sales report — runs every day at 07:00 ─────────────────────

    @Scheduled(cron = "0 0 7 * * *")
    public void sendDailySalesReports() {
        log.info("⏰ Sending daily sales reports...");

        // TODO: inject ReportService and call reportService.buildDailySalesHtml(store, yesterday)
        // For now just a placeholder
        storeRepository.findAll().forEach(store -> {
            String managerEmail = store.getContact() != null
                    ? store.getContact().getEmail() : null;
            if (managerEmail != null) {
                String html = buildPlaceholderDailyReport(store);
                emailService.sendDailySalesReport(store, html);
            }
        });
    }

    // ── Monthly report — runs 1st of every month at 08:00 ────────────────

    @Scheduled(cron = "0 0 8 1 * *")
    public void sendMonthlyReports() {
        log.info("⏰ Sending monthly reports...");

        // TODO: inject ReportService and call reportService.buildMonthlyReportHtml(store, lastMonth)
        storeRepository.findAll().forEach(store -> {
            String html = buildPlaceholderMonthlyReport(store);
            emailService.sendMonthlyReport(store, html);
        });
    }

    // ── Placeholder HTML builders (replace with ReportService later) ──────

    private String buildPlaceholderDailyReport(Store store) {
        return "<html><body style='font-family:Arial,sans-serif'>"
             + "<h2 style='color:#10b981'>Daily Sales Report — " + store.getBrand() + "</h2>"
             + "<p>Connect your ReportService to populate this with real sales data.</p>"
             + "</body></html>";
    }

    private String buildPlaceholderMonthlyReport(Store store) {
        return "<html><body style='font-family:Arial,sans-serif'>"
             + "<h2 style='color:#10b981'>Monthly Report — " + store.getBrand() + "</h2>"
             + "<p>Connect your ReportService to populate this with real monthly data.</p>"
             + "</body></html>";
    }
}