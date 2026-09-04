package com.example.et.config;

import io.lettuce.core.RedisURI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class CacheConfig {
  @Bean
  LettuceConnectionFactory lettuceConnectionFactory(@Value("${redis.url}") String url) {
    // 1. Convert your string URL explicitly into a Lettuce compatible RedisURI
    RedisURI redisUri = RedisURI.create(url);

    // 2. Build explicit Lettuce configuration ensuring SSL is bound
    LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
        .useSsl() // Forces secure SSL/TLS wrapper on the client side
        .build();

    // 3. Create standalone configuration matching your parsed URI parameters
    final var config = LettuceConnectionFactory.createRedisConfiguration(redisUri);

    return new LettuceConnectionFactory(config, clientConfig);
  }

  @Bean
  RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory){
    final var template = new RedisTemplate<String, Object>();
    template.setConnectionFactory(connectionFactory);
    template.setDefaultSerializer(StringRedisSerializer.UTF_8);
    template.afterPropertiesSet();

    return template;
  }

  @Bean
  public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    final var cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofDays(7))
        .disableCachingNullValues();

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(cacheConfiguration)
        .build();
  }
}
