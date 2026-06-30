package com.sh.sh.pos.system.service.RedisService;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtBlackListService {

    private final StringRedisTemplate redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:";

    // BLACKLIST TOKEN
    public void blacklistToken(String token, long expirationMillis) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "logged_out", expirationMillis, TimeUnit.MILLISECONDS);
        log.info("🔒 Token blacklisted — expires in {}s", expirationMillis / 1000);
    }

    //  // CHECK BLACKLIST
    public boolean isBlacklisted(String token) {
           return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
    }

    public void removeFromBlacklist(String token){
      redisTemplate.delete(BLACKLIST_PREFIX + token);
        log.info("🔓 Token removed from blacklist");
    }
    
    // GET REMAINING TTL
     public long getRemainingTime(String token) {

          Long ttl = redisTemplate.getExpire(BLACKLIST_PREFIX + token, TimeUnit.SECONDS);
        return ttl != null ? ttl : 0;
    }
}