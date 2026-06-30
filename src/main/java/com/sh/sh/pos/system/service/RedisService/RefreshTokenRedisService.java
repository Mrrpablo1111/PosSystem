package com.sh.sh.pos.system.service.RedisService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.model.session.UserSession;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service

public class RefreshTokenRedisService {

    private final RedisTemplate<String, UserSession> redisTemplate;
    private static final String REFRESH_PREFIX = "refresh:";

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

        String key = buildKey(email, deviceId);

        UserSession session = new UserSession();
        session.setRefreshToken(refreshToken);
        session.setDeviceId(normalize(deviceId));
        session.setRole(role);
        session.setIp(ip);
        session.setLoginTime(LocalDateTime.now());

        redisTemplate.opsForValue().set(key, session, Duration.ofMillis(expirationMillis));
        log.info("✅ Session saved → email={} device={} expires={}s",
                email, normalize(deviceId), expirationMillis / 1000);
    }

    // =========================
    // VALIDATION
    // =========================
    public boolean isValid(String email, String deviceId, String refreshToken) {
        UserSession session = getSession(email, deviceId);
 
        if (session == null) {
            log.warn("⚠️  Session not found → email={} device={}", email, normalize(deviceId));
            return false;
        }
 
        boolean valid = session.getRefreshToken().equals(refreshToken);
        if (!valid) {
            log.warn("⚠️  Token mismatch → email={} device={}", email, normalize(deviceId));
        }
        return valid;
    }
    // =========================
    // GET SESSION
    // =========================
   public UserSession getSession(String email, String deviceId) {
        return redisTemplate.opsForValue().get(buildKey(email, deviceId));
    }

    // =========================
    // DELETE SESSION (LOGOUT)
    // =========================
    public void delete(String email, String deviceId) {
        redisTemplate.delete(buildKey(email, deviceId));
        log.info("🗑️  Session deleted → email={} device={}", email, normalize(deviceId));
    }

      public void deleteAll(String email) {
        Set<String> keys = redisTemplate.keys(REFRESH_PREFIX + email + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("🗑️  All sessions deleted → email={} count={}", email, keys.size());
        }
    }

    // =========================
    // DEVICE NORMALIZATION
    // =========================

    private String buildKey(String email, String deviceId) {
        return REFRESH_PREFIX + email + ":" + normalize(deviceId);
    }
    private String normalize(String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            return "WEB";
        }
        return deviceId.trim().replaceAll("\\s+", "_").toUpperCase();
    }

    public List<UserSession> getAllSessions(String email) {
        Set<String> keys = redisTemplate.keys(REFRESH_PREFIX + email + ":*");
 
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }
 
        return keys.stream()
                .map(key -> redisTemplate.opsForValue().get(key))
                .filter(session -> session != null)
                .collect(Collectors.toList());
    }
}