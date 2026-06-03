package com.sh.sh.pos.system.service.RedisService;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtBlackListService {

    private final StringRedisTemplate redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:";

    // BLACKLIST TOKEN
    public void blacklistToken(String token, long expirationTime) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(
                key,
                "logged_out",
                expirationTime,
                TimeUnit.MILLISECONDS);
        System.out.println("KEY : " + key);
        System.out.println("EXPIRE IN : " + expirationTime + " ms");
        System.out.println(" TOKEN BLACKLISTED");
    }

    //  // CHECK BLACKLIST
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    public void removeFromBlacklist(String token){
        String key =  BLACKLIST_PREFIX + token;
        redisTemplate.delete(key);
         System.out.println("TOKEN REMOVED FROM BLACKLIST");
    }
    
    // GET REMAINING TTL
     public long getRemainingTime(String token) {

        String key = BLACKLIST_PREFIX + token;

        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);

        return ttl != null ? ttl : 0;
    }
}