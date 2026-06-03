package com.sh.sh.pos.system.configuration;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sh.sh.pos.system.model.session.UserSession;
import com.sh.sh.pos.system.payload.dto.EmailDTO;

@Configuration
public class RedisConfig {

    // ================= CACHE =================
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }

    // ================= SESSION REDIS =================
    @Bean(name = "sessionRedisTemplate")
    public RedisTemplate<String, UserSession> redisTemplate(RedisConnectionFactory factory) {

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

    // ================= EMAIL REDIS =================
    @Bean(name = "emailRedisTemplate")
    public RedisTemplate<String, EmailDTO> emailRedisTemplate(RedisConnectionFactory factory) {

        RedisTemplate<String, EmailDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        configure(template);

        return template;
    }

    // ================= COMMON CONFIG =================
    private void configure(RedisTemplate<?, ?> template) {

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();

        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
    }
}