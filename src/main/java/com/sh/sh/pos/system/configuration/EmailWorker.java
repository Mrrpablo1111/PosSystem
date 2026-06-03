package com.sh.sh.pos.system.configuration;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sh.sh.pos.system.payload.dto.EmailDTO;

import jakarta.mail.internet.MimeMessage;

@Component
public class EmailWorker {

    private final RedisTemplate<String, EmailDTO> redisTemplate;
    private final JavaMailSender mailSender;

    private static final String EMAIL_QUEUE = "email-queue";

    @Scheduled(fixedDelay = 1000)
    public void consumeQueue() {

        EmailDTO dto = redisTemplate.opsForList().rightPop(EMAIL_QUEUE);
        if (dto != null) {
            System.out.print("Found email:" + dto.getTo());
            send(dto);
        }
    }

    public EmailWorker(
            @Qualifier("emailRedisTemplate") RedisTemplate<String, EmailDTO> redisTemplate,
            JavaMailSender mailSender) {
        this.redisTemplate = redisTemplate;
        this.mailSender = mailSender;
    }

    private void send(EmailDTO dto) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            helper.setTo(dto.getTo());
            helper.setSubject(dto.getSubject());
            helper.setText(dto.getBody(), true);

            mailSender.send(message);

        } catch (Exception e) {
           e.printStackTrace();
        }
    }
}