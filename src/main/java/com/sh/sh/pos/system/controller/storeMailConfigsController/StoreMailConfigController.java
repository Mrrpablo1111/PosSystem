package com.sh.sh.pos.system.controller.storeMailConfigsController;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
import com.sh.sh.pos.system.model.Store;
import com.sh.sh.pos.system.model.storeMailConfigs.StoreMailConfig;
import com.sh.sh.pos.system.payload.dto.storeMailConfigsDTO.StoreMailConfigDTO;
import com.sh.sh.pos.system.payload.request.StoreMailConfigRequestDTO;
import com.sh.sh.pos.system.repository.StoreRepository;
import com.sh.sh.pos.system.repository.storeMailConfigsRepository.StoreMailConfigRepository;
import com.sh.sh.pos.system.service.emailService.DynamicMailSender;
import com.sh.sh.pos.system.service.emailService.PasswordEncryptorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
 
@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreMailConfigController {
 
    private final StoreMailConfigRepository mailConfigRepository;
    private final StoreRepository           storeRepository;
    private final PasswordEncryptorService  encryptorService;
    private final DynamicMailSender         dynamicMailSender;
 
    /**
     * POST /api/stores/{storeId}/mail-config
     * Saves the store's Gmail credentials.
     * The App Password is encrypted before saving — never stored as plain text.
     */
    @PostMapping("/{storeId}/mail-config")
    public ResponseEntity<StoreMailConfigDTO> save(
            @PathVariable Long storeId,
            @Valid @RequestBody StoreMailConfigRequestDTO req) {
 
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found: " + storeId));
 
        StoreMailConfig config = mailConfigRepository
                .findByStoreId(storeId)
                .orElse(StoreMailConfig.builder().store(store).build());
 
        config.setFromEmail(req.getFromEmail());
        config.setFromName(req.getFromName() != null
                ? req.getFromName() : store.getBrand());
        config.setSmtpHost(req.getSmtpHost());
        config.setSmtpPort(req.getSmtpPort());
        config.setActive(true);
 
        // ── Encrypt before saving ─────────────────────────────────────────
        config.setEncryptedAppPassword(
                encryptorService.encrypt(req.getAppPassword()));
 
        return ResponseEntity.ok(toDTO(mailConfigRepository.save(config)));
    }
 
    /**
     * GET /api/stores/{storeId}/mail-config
     * Returns config without the password.
     */
    @GetMapping("/{storeId}/mail-config")
    public ResponseEntity<StoreMailConfigDTO> get(@PathVariable Long storeId) {
        return mailConfigRepository.findByStoreId(storeId)
                .map(c -> ResponseEntity.ok(toDTO(c)))
                .orElse(ResponseEntity.notFound().build());
    }
 
    /**
     * PUT /api/stores/{storeId}/mail-config/test
     * Decrypts the password and sends a test email to verify credentials.
     */
    @PutMapping("/{storeId}/mail-config/test")
    public ResponseEntity<String> test(@PathVariable Long storeId) {
 
        StoreMailConfig config = mailConfigRepository
                .findByStoreIdAndActiveTrue(storeId)
                .orElseThrow(() -> new RuntimeException(
                        "No active mail config for store: " + storeId));
 
        // ── Decrypt before sending ────────────────────────────────────────
        String plainPassword = encryptorService.decrypt(
                config.getEncryptedAppPassword());
 
        String testHtml =
                "<h2 style='color:#10b981'>✅ Email Config Test</h2>"
                + "<p>Your Gmail config for <strong>"
                + config.getFromName() + "</strong> is working.</p>";
 
        dynamicMailSender.sendWithPassword(
                config, plainPassword,
                config.getFromEmail(),
                "✅ SH-POS Mail Config Test",
                testHtml);
 
        return ResponseEntity.ok("Test email sent to " + config.getFromEmail());
    }
 
    // ── Mapper — never expose encryptedAppPassword ────────────────────────
 
    private StoreMailConfigDTO toDTO(StoreMailConfig c) {
        return StoreMailConfigDTO.builder()
                .id(c.getId())
                .storeId(c.getStore() != null ? c.getStore().getId() : null)
                .fromEmail(c.getFromEmail())
                .fromName(c.getFromName())
                .smtpHost(c.getSmtpHost())
                .smtpPort(c.getSmtpPort())
                .active(c.getActive())
                // encryptedAppPassword is NEVER included in DTO
                .build();
    }
}