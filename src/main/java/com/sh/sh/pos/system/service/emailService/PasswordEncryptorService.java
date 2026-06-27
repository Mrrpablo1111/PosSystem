package com.sh.sh.pos.system.service.emailService;

import org.springframework.stereotype.Service;
 
import lombok.RequiredArgsConstructor;
 
/**
 * Thin wrapper — delegates to AesEncryptorService.
 * Keeps the rest of the code unchanged.
 */
@Service
@RequiredArgsConstructor
public class PasswordEncryptorService {
 
    private final AesEncryptorService aes;
 
    public String encrypt(String plainText) {
        return aes.encrypt(plainText);
    }
 
    public String decrypt(String encryptedText) {
        return aes.decrypt(encryptedText);
    }
}
 