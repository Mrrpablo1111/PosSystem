package com.sh.sh.pos.system.configuration;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sh.sh.pos.system.model.session.UserSession;
import com.sh.sh.pos.system.payload.dto.EmailDTO;

@Configuration
public class RedisConfig {

    // ── Cache ─────────────────────────────────────────────────────────────

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10));
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }

    // ── Session Redis ─────────────────────────────────────────────────────

    @Bean(name = "sessionRedisTemplate")
    public RedisTemplate<String, UserSession> sessionRedisTemplate(
            RedisConnectionFactory factory) {

        RedisTemplate<String, UserSession> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonRedisSerializer<UserSession> serializer =
                new Jackson2JsonRedisSerializer<>(mapper, UserSession.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();

        return template;
    }

    // ── Email Redis ───────────────────────────────────────────────────────

    /**
     * FIX: use Jackson2JsonRedisSerializer<EmailDTO> with explicit type
     * instead of GenericJackson2JsonRedisSerializer.
     *
     * GenericJackson2JsonRedisSerializer adds @class metadata to JSON which
     * can cause deserialization issues when new fields are added.
     *
     * Jackson2JsonRedisSerializer<EmailDTO> serializes cleanly:
     * {"to":"...","subject":"...","body":"...","storeId":102,"purchaseOrderId":13}
     */
    @Bean(name = "emailRedisTemplate")
    public RedisTemplate<String, EmailDTO> emailRedisTemplate(
            RedisConnectionFactory factory) {

        RedisTemplate<String, EmailDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Allow unknown fields so old messages don't break new versions
        mapper.configure(
                com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);

        Jackson2JsonRedisSerializer<EmailDTO> serializer =
                new Jackson2JsonRedisSerializer<>(mapper, EmailDTO.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();

        return template;
    }
}