package com.sh.sh.pos.system.service.redisservice;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Service
@RequiredArgsConstructor
public class AccountVerificationRedisService {

    private static final String OTP_PREFIX = "account:verification:otp:";

    private static final String OTP_ATTEMPTS_PREFIX = "account:verification:otp:attempts:";

    private static final long OTP_EXPIRATION_MINUTES = 5;
    private static final int MAX_OTP_ATTEMPTS = 5;

    private final RedisTemplate<String, String> redisTemplate;

    // Save accout verification OTP
    public void saveOtp(String identifier, String otp) {
        String key = OTP_PREFIX + identifier;
        redisTemplate.opsForValue().set(key, otp, OTP_EXPIRATION_MINUTES, TimeUnit.MINUTES);

        // Reset Attempts whenever a new otp is issued
        redisTemplate.delete(OTP_ATTEMPTS_PREFIX + identifier);

        log.info("Account verification OTP saved for identifier={}", maskIdentifier(identifier));
    }

    // Get account verification OTP
    
    public String getOtp(String identifier){
        return redisTemplate.opsForValue().get(OTP_PREFIX + identifier);
    }

    //  Check whether account verification OTP exists
    public boolean otpExists(String identifier){
        return Boolean.TRUE.equals(redisTemplate.hasKey(OTP_PREFIX + identifier));
    }

    // Delete OTP and attempts
    public void deleteOtp(String identifier){
        redisTemplate.delete(OTP_PREFIX + identifier);
        redisTemplate.delete(OTP_ATTEMPTS_PREFIX + identifier);
    }

    // Increment OTP attempts

    public long incrementOtpAttempts(String identifier){
        String key = OTP_ATTEMPTS_PREFIX + identifier;
        Long attempts = redisTemplate.opsForValue().increment(key);

        if(attempts != null && attempts == 1){
            redisTemplate.expire(key, OTP_EXPIRATION_MINUTES, TimeUnit.MINUTES);
        }

        return attempts != null ? attempts : 0;
    } 

    // Check whether maximum number of attempts has been reached
    public boolean hasExceededOtpAttempts(String identifier){
        String value = redisTemplate.opsForValue().get(OTP_ATTEMPTS_PREFIX + identifier);
        if(value == null){
            return false;
        }
        try{
            return Integer.parseInt(value) >= MAX_OTP_ATTEMPTS;
        }catch(NumberFormatException e){
            log.error("Failed to parse OTP attempts for identifier={}", maskIdentifier(identifier), e);
            return true;

        }
    }

    // Mask identifier for logging
    private String maskIdentifier(String identifier){
        if(identifier == null || identifier.length() <4){
            return "****";
        }
        if(identifier.contains("@")){
            int atIndex = identifier.indexOf("@");
            if(atIndex <=1){
                return "***" + identifier.substring(atIndex);
            }

            return identifier.substring(0,2) + "***" + identifier.substring(atIndex);
        }
        return "***" + identifier.substring(Math.max(0, identifier.length() -4 ));
    }

    

}
