package com.sh.sh.pos.system.controller.devEmailController;



import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.scheduler.EmailScheduler;

import lombok.RequiredArgsConstructor;

/**
 * DEV ONLY — manually trigger scheduled email jobs via Postman.
 * Only active when spring.profiles.active=dev
 * Remove or comment out before deploying to production.
 */
@RestController
@RequestMapping("/api/dev/email")
@Profile("dev")
@RequiredArgsConstructor
public class DevEmailController {

    private final EmailScheduler emailScheduler;

    /** POST /api/dev/email/trigger-overdue */
    @PostMapping("/trigger-overdue")
    public ResponseEntity<String> triggerOverdue() {
        emailScheduler.checkOverduePurchaseOrders();
        return ResponseEntity.ok("Overdue check triggered");
    }

    /** POST /api/dev/email/trigger-low-stock */
    @PostMapping("/trigger-low-stock")
    public ResponseEntity<String> triggerLowStock() {
        emailScheduler.checkLowStock();
        return ResponseEntity.ok("Low stock check triggered");
    }

    /** POST /api/dev/email/trigger-expiry */
    @PostMapping("/trigger-expiry")
    public ResponseEntity<String> triggerExpiry() {
        emailScheduler.checkExpiringBatches();
        return ResponseEntity.ok("Expiry check triggered");
    }

    /** POST /api/dev/email/trigger-daily-report */
    @PostMapping("/trigger-daily-report")
    public ResponseEntity<String> triggerDailyReport() {
        emailScheduler.sendDailySalesReports();
        return ResponseEntity.ok("Daily report triggered");
    }

    /** POST /api/dev/email/trigger-monthly-report */
    @PostMapping("/trigger-monthly-report")
    public ResponseEntity<String> triggerMonthlyReport() {
        emailScheduler.sendMonthlyReports();
        return ResponseEntity.ok("Monthly report triggered");
    }
}