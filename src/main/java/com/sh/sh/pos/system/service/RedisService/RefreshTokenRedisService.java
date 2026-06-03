package com.sh.sh.pos.system.service.RedisService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.model.session.UserSession;



@Service

public class RefreshTokenRedisService {

    private final RedisTemplate<String, UserSession> redisTemplate;
    public RefreshTokenRedisService(
            @Qualifier("sessionRedisTemplate") RedisTemplate<String, UserSession> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
  
    private String buildKey(String email, String deviceId) {
        return "refresh:" + email + ":" + normalize(deviceId);
    }

    // =========================
    // SIMPLE SAVE (LOGIN USE)
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

        redisTemplate.opsForValue().set(
                key,
                session,
                Duration.ofMillis(expirationMillis)
        );
    }

    // =========================
    // VALIDATION
    // =========================
    public boolean isValid(String email, String deviceId, String refreshToken) {

    String key = buildKey(email, deviceId);

    System.out.println("Checking Redis key: " + key);

    UserSession session = redisTemplate.opsForValue().get(key);

    System.out.println("Session: " + session);

    if(session != null){
        System.out.println("Stored Token: " + session.getRefreshToken());
        System.out.println("Incoming Token: " + refreshToken);
    }

    return session != null &&
            session.getRefreshToken().equals(refreshToken);
}

    // =========================
    // GET SESSION
    // =========================
    public UserSession getSession(String email, String deviceId) {

        String key = buildKey(email, deviceId);

        return redisTemplate.opsForValue().get(key);
    }

    // =========================
    // DELETE SESSION (LOGOUT)
    // =========================
    public void delete(String email, String deviceId) {

        String key = buildKey(email, deviceId);

        redisTemplate.delete(key);
    }

    // =========================
    // DEVICE NORMALIZATION
    // =========================
    private String normalize(String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            return "WEB";
        }
        return deviceId.trim().replaceAll("\\s+", "_").toUpperCase();
    }

    public List<UserSession> getAllSessions(String email){
        Set<String> keys = redisTemplate.keys("refresh:" + email + ":*");

        List<UserSession> sessions = new ArrayList<>();
        
        if(keys != null){
            for(String key : keys){
                UserSession session = redisTemplate.opsForValue().get(key);

                if(session != null){
                    sessions.add(session);
                }
            }
        }
        return sessions;
    }
}