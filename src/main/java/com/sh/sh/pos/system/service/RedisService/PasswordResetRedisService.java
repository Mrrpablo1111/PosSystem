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

    private static final long EXPIRATION_MINUTES = 15;
    private static final String RESET_PREFIX = "reset:";

    private final RedisTemplate<String, String> redisTemplate;

    public void saveToken(String token, String email) {
        log.info("-> Saving reset token for email={}", email);
        redisTemplate.opsForValue().set(
                RESET_PREFIX + token,
                email,
                EXPIRATION_MINUTES,
                TimeUnit.MINUTES);
    }

    public String getEmail(String token) {
        return redisTemplate.opsForValue().get(RESET_PREFIX + token);
    }

    public void deleteToken(String token) {
        redisTemplate.delete(RESET_PREFIX + token);
    }

    public boolean isValid(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(RESET_PREFIX + token));
    }
}