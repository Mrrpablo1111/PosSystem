package com.sh.sh.pos.system.service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetRedisService {
     private final RedisTemplate<String, String> redisTemplate;

    private static final long EXPIRATION_TIME = 15; // minutes

    public void saveToken(String token, String email) {
         System.out.println("🔥 SAVING RESET TOKEN");
        redisTemplate.opsForValue().set(
            "reset:" + token,
            email,
            EXPIRATION_TIME,
            TimeUnit.MINUTES
        );
        System.out.println("🔥 SAVED: reset:" + token);
    }

    public String getEmail(String token) {
        return redisTemplate.opsForValue().get("reset:" + token);
    }

    public void deleteToken(String token) {
        redisTemplate.delete("reset:" + token);
    }

    public boolean isValid(String token) {
        return redisTemplate.hasKey("reset:" + token);
    }
}
