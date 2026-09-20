package com.sh.sh.pos.system.service.serviceimpl.aesencryptorservice;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.sh.sh.pos.system.exception.EncryptionException;
import com.sh.sh.pos.system.service.emailservice.AesEncryptorService;

@DisplayName("AesEncryptorService — AES/GCM round-trip and tamper detection")
class AesEncryptorServiceTest {

    private AesEncryptorService encryptor;

    @BeforeEach
    void setUp() {
        encryptor = new AesEncryptorService();
        // inject the @Value field the way Spring would
        ReflectionTestUtils.setField(encryptor, "secretKey", "test-secret-key-for-unit-tests-32");
    }

    @Test
    @DisplayName("encrypt → decrypt returns the original plaintext ✅")
    void roundTrip() {
        String plain = "my-smtp-app-password";

        String cipher = encryptor.encrypt(plain);
        String decrypted = encryptor.decrypt(cipher);

        assertThat(decrypted).isEqualTo(plain);
    }

    @Test
    @DisplayName("same plaintext encrypted twice → different ciphertexts (random IV) ✅")
    void randomIvPerEncryption() {
        String plain = "repeatable-value";

        String first = encryptor.encrypt(plain);
        String second = encryptor.encrypt(plain);

        assertThat(first).isNotEqualTo(second);
        // ...but both still decrypt correctly
        assertThat(encryptor.decrypt(first)).isEqualTo(plain);
        assertThat(encryptor.decrypt(second)).isEqualTo(plain);
    }

    @Test
    @DisplayName("tampered ciphertext → EncryptionException, not garbage output ✅")
    void tamperDetection() {
        String cipher = encryptor.encrypt("sensitive");

        byte[] bytes = Base64.getDecoder().decode(cipher);
        bytes[bytes.length - 1] ^= 0x01; // flip one bit in the auth tag
        String tampered = Base64.getEncoder().encodeToString(bytes);

        assertThatThrownBy(() -> encryptor.decrypt(tampered))
                .isInstanceOf(EncryptionException.class);
    }

    @Test
    @DisplayName("decrypt with a different key → EncryptionException ✅")
    void wrongKeyRejected() {
        String cipher = encryptor.encrypt("secret-value");

        AesEncryptorService other = new AesEncryptorService();
        ReflectionTestUtils.setField(other, "secretKey", "a-completely-different-key-here!");

        assertThatThrownBy(() -> other.decrypt(cipher))
                .isInstanceOf(EncryptionException.class);
    }

    @Test
    @DisplayName("null and blank inputs → null out, no exception ✅")
    void nullAndBlankHandling() {
        assertThat(encryptor.encrypt(null)).isNull();
        assertThat(encryptor.encrypt("  ")).isNull();
        assertThat(encryptor.decrypt(null)).isNull();
        assertThat(encryptor.decrypt("")).isNull();
    }

    @Test
    @DisplayName("garbage (non-Base64) input to decrypt → EncryptionException ✅")
    void invalidBase64Rejected() {
        assertThatThrownBy(() -> encryptor.decrypt("not-valid-base64-%%%"))
                .isInstanceOf(EncryptionException.class);
    }

    @Test
    @DisplayName("short key gets padded — still round-trips ✅")
    void shortKeyPadded() {
        AesEncryptorService shortKey = new AesEncryptorService();
        ReflectionTestUtils.setField(shortKey, "secretKey", "short");

        String cipher = shortKey.encrypt("hello");
        assertThat(shortKey.decrypt(cipher)).isEqualTo("hello");
    }
}