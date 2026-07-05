package com.sh.sh.pos.system.scheduler;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sh.sh.pos.system.domain.PurchaseOrderStatus;
import com.sh.sh.pos.system.model.branch.Branch;
import com.sh.sh.pos.system.model.products.ProductBatch;
import com.sh.sh.pos.system.model.purchasesorder.PurchaseOrder;
import com.sh.sh.pos.system.model.store.Store;
import com.sh.sh.pos.system.model.storecontact.StoreContact;
import com.sh.sh.pos.system.repository.productbatchrepository.ProductBatchRepository;
import com.sh.sh.pos.system.repository.purchasesorderrepository.PurchaseOrderRepository;
import com.sh.sh.pos.system.repository.storerepository.StoreRepository;
import com.sh.sh.pos.system.service.emailservice.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailScheduler {

    private final EmailService emailService;
    private final PurchaseOrderRepository poRepository;
    private final ProductBatchRepository batchRepository;
    private final StoreRepository storeRepository;

    /** Minimum remaining quantity before low-stock alert fires */
    private static final int LOW_STOCK_THRESHOLD = 10;

    /** Days before expiry to start alerting */
    private static final int EXPIRY_DAYS_WARNING = 30;

    // ── Overdue PO check — runs every day at 08:00 ────────────────────────
    @Transactional
    @Scheduled(cron = "0 0 8 * * *")
    public void checkOverduePurchaseOrders() {
        log.info("⏰ Running overdue PO check...");

        List<PurchaseOrder> overdue = poRepository.findOverdue(LocalDate.now());

        overdue.forEach(po -> {
            // Mark as OVERDUE
            po.setStatus(PurchaseOrderStatus.OVERDUE);
            poRepository.save(po);

            // Notify store manager via store contact email
            String managerEmail = resolveManagerEmail(po);
            emailService.sendOverduePOAlert(po, managerEmail);
        });

        log.info("⏰ Overdue PO check done — {} POs flagged", overdue.size());
    }

    // ── Low stock check — runs every day at 07:00 ─────────────────────────

    @Scheduled(cron = "0 0 7 * * *")
    public void checkLowStock() {
        log.info("⏰ Running low stock check...");

        storeRepository.findAll().forEach(store -> {
            String managerEmail = resolveManagerEmail(store);
            if (managerEmail == null || managerEmail.isBlank()) {
                return;
            }

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

        storeRepository.findAll().forEach(store -> {
            String managerEmail = resolveManagerEmail(store);
            if (managerEmail == null || managerEmail.isBlank()) {
                return;
            }

            List<ProductBatch> expiring = batchRepository
                    .findByBranchStoreIdAndExpiryDateBefore(store.getId(), warningDate)
                    .stream()
                    .filter(b -> b.getRemainingQuantity() > 0)
                    .toList();

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

        // For now just a placeholder
        storeRepository.findAll().forEach(store -> {
            String managerEmail = store.getContact() != null
                    ? store.getContact().getEmail()
                    : null;
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

    private static String resolveManagerEmail(PurchaseOrder po) {
        return Optional.ofNullable(po.getBranch())
                .map(Branch::getStore)
                .map(EmailScheduler::resolveManagerEmail)
                .orElse(null);
    }

    private static String resolveManagerEmail(Store store) {
        return Optional.ofNullable(store)
                .map(Store::getContact)
                .map(StoreContact::getEmail)
                .orElse(null);
    }
}
