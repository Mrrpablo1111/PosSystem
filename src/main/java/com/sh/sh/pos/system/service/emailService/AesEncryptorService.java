package com.sh.sh.pos.system.service.emailservice;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.exception.EncryptionException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AesEncryptorService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final String KEY_ALG   = "AES";

    private static final int KEY_LENGTH_BYTES    = 32;  // AES-256
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${app.encrypt.secret}")
    private String secretKey;

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            return null;
        }
        try {
            byte[] ivBytes = new byte[GCM_IV_LENGTH_BYTES];
            SECURE_RANDOM.nextBytes(ivBytes);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec(),
                    new GCMParameterSpec(GCM_TAG_LENGTH_BITS, ivBytes));

            byte[] encrypted = cipher.doFinal(
                    plainText.trim().getBytes(StandardCharsets.UTF_8));

            // Prepend the IV so decrypt() can recover it
            byte[] combined = ByteBuffer.allocate(ivBytes.length + encrypted.length)
                    .put(ivBytes)
                    .put(encrypted)
                    .array();

            log.debug("✅ Encryption successful");
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("❌ Encryption error: {}", e.getMessage(), e);
            throw new EncryptionException("Encryption failed", e);
        }
    }

    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isBlank()) {
            return null;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedText);

            ByteBuffer buffer = ByteBuffer.wrap(combined);
            byte[] ivBytes = new byte[GCM_IV_LENGTH_BYTES];
            buffer.get(ivBytes);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec(),
                    new GCMParameterSpec(GCM_TAG_LENGTH_BITS, ivBytes));

            byte[] decrypted = cipher.doFinal(encrypted);
            log.debug("✅ Decryption successful");
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("❌ Decryption error: {}", e.getMessage(), e);
            throw new EncryptionException("Decryption failed", e);
        }
    }

    private SecretKeySpec keySpec() {
        // Pad or trim to exactly 32 bytes automatically
        byte[] raw = secretKey.getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[KEY_LENGTH_BYTES];
        System.arraycopy(raw, 0, key, 0, Math.min(raw.length, KEY_LENGTH_BYTES));
        return new SecretKeySpec(key, KEY_ALG);
    }
}