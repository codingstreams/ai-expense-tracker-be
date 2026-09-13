package com.example.et.module.ai.chat;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class CacheConfig {
  @Bean
  RedisTemplate<String, StoredChatMessage> redisTemplate(LettuceConnectionFactory connectionFactory) {
    final var template = new RedisTemplate<String, StoredChatMessage>();
    template.setConnectionFactory(connectionFactory);
    template.setKeySerializer(RedisSerializer.string());
    template.setValueSerializer(RedisSerializer.json());
    template.afterPropertiesSet();

    return template;
  }

}
