package com.sh.sh.pos.system.service.redisservice;

import java.time.Duration;
import java.time.LocalDateTime;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.model.session.UserSession;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service

public class RefreshTokenRedisService {

    private static final String REFRESH_PREFIX = "refresh:";
    private static final String DEFAULT_DEVICE_ID = "WEB";
    private final RedisTemplate<String, UserSession> redisTemplate;

    public RefreshTokenRedisService(
            @Qualifier("sessionRedisTemplate") RedisTemplate<String, UserSession> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // =========================
    // SIMPLE SAVE (LOGIN USE) - used during login when role/IP are not available
    // yet.
    // =========================
    public void save(
            String email,
            String deviceId,
            String refreshToken,
            long expirationMillis) {

        save(email, deviceId, "UNKNOWN", "UNKNOWN", refreshToken, expirationMillis);
    }

    // =========================
    // FULL SAVE (ADVANCED)
    // =========================
    public void save(
            String email,
            String deviceId,
            String role,
            String ip,
            String refreshToken,
            long expirationMillis) {

        String normalizedEmail = normalizeEmail(email);
        String normalizedDeviceId = normalizeDeviceId(deviceId);

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token cannot be null or blank");
        }
        if (expirationMillis <= 0) {
            throw new IllegalArgumentException("Expiration time must be greater than zero");
        }

        String normalizedToken = refreshToken.trim();

        String key = buildKey(normalizedEmail, normalizedDeviceId);

        UserSession session = new UserSession();
        session.setRefreshToken(normalizedToken);
        session.setDeviceId(normalize(normalizedDeviceId));
        session.setRole(role == null || role.isBlank() ? "UNKNOWN" : role.trim());
        session.setIp(ip == null || ip.isBlank() ? "UNKNOWN" : ip.trim());
        session.setLoginTime(LocalDateTime.now());

        redisTemplate.opsForValue().set(key, session, Duration.ofMillis(expirationMillis));
        log.info("✅ Session saved → email={} device={} expires={}s",
                email, normalize(deviceId), expirationMillis / 1000);
    }

    // =========================
    // VALIDATION
    // =========================
    public boolean isValid(String email, String deviceId, String refreshToken) {

        String normalizedEmail = normalizeEmail(email);
        String normalizedDeviceId = normalizeDeviceId(deviceId);

        if (refreshToken == null || refreshToken.isBlank()) {
            log.warn("Refresh Token missing -> email={} device={}", normalizedEmail, normalizedDeviceId);
            return false;
        }

        String normalizedToken = refreshToken.trim();

        UserSession session = getSession(email, deviceId);
        if (session == null) {
            log.warn("⚠️  Session not found → email={} device={}", normalizedEmail, normalizedDeviceId);
            return false;
        }

        String storedToken = session.getRefreshToken();
        if (storedToken == null || storedToken.isBlank()) {
            log.warn("Refresh session has no token -> email={} device={}", normalizedEmail, normalizedDeviceId);
            return false;
        }

       boolean valid = storedToken.equals(normalizedToken);

        if (!valid) {
            log.warn("Refresh token mismatch → email={} device={}", normalizedEmail, normalizedDeviceId);
            return false;
        }
        log.debug("Refresh token valid → email={} device={}", normalizedEmail, normalizedDeviceId);
        return true;
    }

    // =========================
    // GET SESSION
    // =========================
    public UserSession getSession(String email, String deviceId) {

        String normalizedEmail = normalizeEmail(email);
        String normalizedDeviceId = normalizeDeviceId(deviceId);

        String key = buildKey(normalizedEmail, normalizedDeviceId);

        return redisTemplate.opsForValue().get(key);
    }

    public String getRefreshToken(String email, String deviceId) {
        UserSession session = getSession(email, deviceId);
        if (session == null) {
            return null;
        }

        return session.getRefreshToken();
    }

    // =========================
    // DELETE SESSION (LOGOUT)
    // =========================
    public void delete(String email, String deviceId) {

        String normalizedEmail = normalizeEmail(email);
        String normalizedDeviceId = normalizeDeviceId(deviceId);

        String key = buildKey(normalizedEmail, normalizedDeviceId);

        Boolean deleted = redisTemplate.delete(key);

        if (Boolean.TRUE.equals(deleted)) {
            log.info("Refresh session -> email={} device={}", normalizedEmail, normalizedDeviceId);
        } else {
            log.debug("Refresh session already absent-> email={} device={}", normalizedEmail, normalizedDeviceId);
        }
    }

    public void deleteAll(String email) {

        String normalizedEmail = normalizeEmail(email);

        Set<String> keys = redisTemplate.keys(REFRESH_PREFIX + normalizedEmail + ":*");
        if (keys == null && keys.isEmpty()) {

            log.info("No refresh sessions found → email={}\", normalizedEmail");
            return;
        }

        redisTemplate.delete(keys);
        log.info("All refresh sessions deleted → email={} count={}", normalizedEmail, keys.size());
    }

    // =========================
    // DEVICE NORMALIZATION
    // =========================

    private String buildKey(String email, String deviceId) {
        return REFRESH_PREFIX + normalizeEmail(email) + ":" + normalizeDeviceId(deviceId);
    }

    private String normalize(String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            return "WEB";
        }
        return deviceId.trim().replaceAll("\\s+", "_").toUpperCase();
    }

    public List<UserSession> getAllSessions(String email) {
        String normalizedEmail = normalizeEmail(email);
        Set<String> keys = redisTemplate.keys(REFRESH_PREFIX + normalizedEmail + ":*");

        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }

        return keys.stream().map(key -> redisTemplate.opsForValue().get(key)).filter(session -> session != null)
                .toList();
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        return email.trim().toLowerCase();
    }

    private String normalizeDeviceId(String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            return DEFAULT_DEVICE_ID;
        }
        return deviceId.trim().replaceAll("\\s+", "_").toUpperCase();
    }

    

}
