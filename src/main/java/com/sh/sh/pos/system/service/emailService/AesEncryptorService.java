package com.sh.sh.pos.system.service.emailService;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AesEncryptorService {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALG   = "AES";

    @Value("${app.encrypt.secret}")
    private String secretKey;

    @Value("${app.encrypt.iv}")
    private String iv;

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) return null;
        try {
            // Log lengths so you can see exactly what is loaded
            log.info("🔑 secret length={} iv length={}",
                    secretKey.length(), iv.length());

            SecretKeySpec   keySpec = keySpec();
            IvParameterSpec ivSpec  = ivSpec();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(
                    plainText.trim().getBytes(StandardCharsets.UTF_8));
            String result = Base64.getEncoder().encodeToString(encrypted);
            log.info("✅ Encryption successful");
            return result;
        } catch (Exception e) {
            log.error("❌ Encryption error: {} | secret.length={} iv.length={}",
                    e.getMessage(), secretKey.length(), iv.length(), e);
            throw new RuntimeException("Encryption failed: " + e.getMessage(), e);
        }
    }

    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isBlank()) return null;
        try {
            SecretKeySpec   keySpec = keySpec();
            IvParameterSpec ivSpec  = ivSpec();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decoded   = Base64.getDecoder().decode(encryptedText);
            byte[] decrypted = cipher.doFinal(decoded);
            log.info("✅ Decryption successful");
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("❌ Decryption error: {} | secret.length={} iv.length={}",
                    e.getMessage(), secretKey.length(), iv.length(), e);
            throw new RuntimeException("Decryption failed: " + e.getMessage(), e);
        }
    }

    private SecretKeySpec keySpec() {
        // Pad or trim to exactly 32 bytes automatically
        byte[] raw = secretKey.getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[32];
        System.arraycopy(raw, 0, key, 0, Math.min(raw.length, 32));
        return new SecretKeySpec(key, KEY_ALG);
    }

    private IvParameterSpec ivSpec() {
        // Pad or trim to exactly 16 bytes automatically
        byte[] raw   = iv.getBytes(StandardCharsets.UTF_8);
        byte[] ivArr = new byte[16];
        System.arraycopy(raw, 0, ivArr, 0, Math.min(raw.length, 16));
        return new IvParameterSpec(ivArr);
    }
}