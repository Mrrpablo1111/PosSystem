package com.sh.sh.pos.system.service.emailService;

import java.util.Properties;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.model.storeMailConfigs.StoreMailConfig;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicMailSender {

    private final PasswordEncryptorService encryptorService;

    /**
     * Send HTML only — no PDF.
     * Used by the test endpoint.
     */
    public void send(StoreMailConfig config, String to, String subject, String html) {
        String plainPassword = encryptorService.decrypt(config.getEncryptedAppPassword());
        sendWithPassword(config, plainPassword, to, subject, html);
    }

    /**
     * Send HTML + optional PDF attachment.
     * Called by EmailWorker for PO emails.
     *
     * @param pdfBytes null = no attachment
     * @param pdfName  filename shown in email e.g. "PurchaseOrder-13.pdf"
     */
    public void sendWithPdf(StoreMailConfig config,
                            String to,
                            String subject,
                            String html,
                            byte[] pdfBytes,
                            String pdfName) {
        try {
            String plainPassword = encryptorService.decrypt(config.getEncryptedAppPassword());
            JavaMailSenderImpl sender = buildSender(config, plainPassword);

            MimeMessage       message = sender.createMimeMessage();
            MimeMessageHelper helper  = new MimeMessageHelper(message, true, "UTF-8");

            String fromName = config.getFromName() != null
                    ? config.getFromName() : config.getFromEmail();

            helper.setFrom(config.getFromEmail(), fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            // ── Attach PDF if provided ────────────────────────────────────
            if (pdfBytes != null && pdfBytes.length > 0 && pdfName != null) {
                helper.addAttachment(
                        pdfName,
                        new ByteArrayResource(pdfBytes),
                        "application/pdf");
                log.info("📎 PDF attached: {} ({} bytes)", pdfName, pdfBytes.length);
            }

            sender.send(message);
            log.info("✅ Email sent from={} to={} attachment={}",
                    config.getFromEmail(), to,
                    pdfName != null ? pdfName : "none");

        } catch (Exception e) {
            log.error("❌ sendWithPdf failed from={} to={} error={}",
                    config.getFromEmail(), to, e.getMessage(), e);
            throw new RuntimeException("Email send failed: " + e.getMessage(), e);
        }
    }

    /**
     * Send with already-decrypted password — used by test endpoint.
     */
    public void sendWithPassword(StoreMailConfig config,
                                  String plainPassword,
                                  String to,
                                  String subject,
                                  String html) {
        try {
            JavaMailSenderImpl sender = buildSender(config, plainPassword);

            MimeMessage       message = sender.createMimeMessage();
            MimeMessageHelper helper  = new MimeMessageHelper(message, true, "UTF-8");

            String fromName = config.getFromName() != null
                    ? config.getFromName() : config.getFromEmail();

            helper.setFrom(config.getFromEmail(), fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            sender.send(message);
            log.info("✅ Email sent from={} to={}", config.getFromEmail(), to);

        } catch (Exception e) {
            log.error("❌ Email failed from={} to={} error={}",
                    config.getFromEmail(), to, e.getMessage(), e);
            throw new RuntimeException("Email send failed: " + e.getMessage(), e);
        }
    }

    // ── Build JavaMailSender from store config ────────────────────────────

    private JavaMailSenderImpl buildSender(StoreMailConfig config, String plainPassword) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();

        sender.setHost(config.getSmtpHost() != null
                ? config.getSmtpHost() : "smtp.gmail.com");
        sender.setPort(config.getSmtpPort() != null
                ? config.getSmtpPort() : 587);
        sender.setUsername(config.getFromEmail());
        sender.setPassword(plainPassword);

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol",     "smtp");
        props.put("mail.smtp.auth",              "true");
        props.put("mail.smtp.starttls.enable",   "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.trust",         "smtp.gmail.com");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout",           "5000");
        props.put("mail.smtp.writetimeout",      "5000");

        return sender;
    }
}