package com.sh.sh.pos.system.service.redisservice;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetRedisService {

    private static final String OTP_PREFIX = "password:otp:";
    private static final String OTP_ATTEMPTS_PREFIX = "password:otp:attempts:";

    private static final long OTP_EXPIRATION_MINUTES = 5;
    private static final long RESET_TOKEN_EXPIRATION_MINUTES = 10;
    private static final String RESET_PREFIX = "password:reset:";

    private static final int MAX_OTP_ATTEMPTS = 5;

    private final RedisTemplate<String, String> redisTemplate;

    // OTP
    public void saveOtp(String identifier, String otp) {
        String key = OTP_PREFIX + identifier;

        redisTemplate.opsForValue().set(key, otp, OTP_EXPIRATION_MINUTES, TimeUnit.MINUTES);
        // Reset attempts whenever a new OTP is generated
        redisTemplate.delete(OTP_ATTEMPTS_PREFIX + identifier);

        log.info("Password reset OTP saved for identifier={}", maskIdentifier(identifier));
    }

    public String getOtp(String identifier) {
        return redisTemplate.opsForValue().get(OTP_PREFIX + identifier);
    }

    public boolean otpExists(String identifier) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(OTP_PREFIX + identifier));
    }

    public void deleteOtp(String identifier) {
        redisTemplate.delete(OTP_PREFIX + identifier);
        redisTemplate.delete(OTP_ATTEMPTS_PREFIX + identifier);
    }

    // OTP ATTEMPTS
    public long incrementOtpAttempts(String identifier) {
        String key = OTP_ATTEMPTS_PREFIX + identifier;
        Long attempts = redisTemplate.opsForValue().increment(key);
        if (attempts != null && attempts == 1) {
            redisTemplate.expire(key, OTP_EXPIRATION_MINUTES, TimeUnit.MINUTES);
        }
        return attempts == null ? 0 : attempts;
    }

    public boolean hasExceededOtpAttempts(String identifier) {
        String value = redisTemplate.opsForValue().get(OTP_ATTEMPTS_PREFIX + identifier);
        if (value == null) {
            return false;
        }
        try {
            int attempts = Integer.parseInt(value);
            return attempts >= MAX_OTP_ATTEMPTS;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void saveResetToken(String resetToken, String email) {
        redisTemplate.opsForValue().set(RESET_PREFIX + resetToken, email, RESET_TOKEN_EXPIRATION_MINUTES,
                TimeUnit.MINUTES);
        log.info("Password reset token saved for email={}", email);
    }

    public String getEmailFromResetToken(String resetToken) {
        return redisTemplate.opsForValue().get(RESET_PREFIX + resetToken);
    }

    public boolean isResetTokenValid(String resetToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(RESET_PREFIX + resetToken));
    }

    public void deleteResetToken(String resetToken) {
        redisTemplate.delete(RESET_PREFIX + resetToken);
    }

    public void deleteAll(String identifier, String resetToken) {
        deleteOtp(identifier);
        if (resetToken != null && !resetToken.isBlank()) {
            deleteResetToken(resetToken);
        }
    }

    private String maskIdentifier(String identifier) {
        if (identifier == null || identifier.length() < 4) {
            return "****";
        }
        if (identifier.contains("@")) {
            int atIndex = identifier.indexOf("@");
            if (atIndex <= 2) {
                return "***" + identifier.substring(atIndex);
            }
            return identifier.substring(0, 2) + "***" + identifier.substring(atIndex);
        }
        return "***" + identifier.substring(Math.max(0, identifier.length() - 4));
    }
}