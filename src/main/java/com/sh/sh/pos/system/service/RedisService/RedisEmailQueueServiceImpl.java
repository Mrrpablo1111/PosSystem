package com.sh.sh.pos.system.service.RedisService;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.model.session.UserSession;
import com.sh.sh.pos.system.payload.dto.EmailDTO;
import com.sh.sh.pos.system.service.EmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisEmailQueueServiceImpl implements EmailService {

    @Qualifier("emailRedisTemplate")

    private final RedisTemplate<String, EmailDTO> redisTemplate;

    private static final String EMAIL_QUEUE = "email-queue";

    @Override
    public void sendEmail(String to, String subject, String body) {

        EmailDTO job = new EmailDTO(to, subject, body);

        redisTemplate.opsForList().leftPush(EMAIL_QUEUE, job);

        System.out.println("📩 Email queued for: " + to);
    }
}