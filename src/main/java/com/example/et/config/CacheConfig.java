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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class CacheConfig {
  @Bean
  LettuceConnectionFactory lettuceConnectionFactory(@Value("${redis.url}") String url) {
    RedisURI redisUri = RedisURI.create(url);

    LettuceClientConfiguration.LettuceClientConfigurationBuilder clientConfigBuilder =
        LettuceClientConfiguration.builder();

    if (redisUri.isSsl()) {
      clientConfigBuilder.useSsl();
    }

    LettuceClientConfiguration clientConfig = clientConfigBuilder.build();
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
  StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory connectionFactory) {
    return new StringRedisTemplate(connectionFactory);
  }

  @Bean
  public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    final var cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofDays(7))
        .disableCachingNullValues()
        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()));

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(cacheConfiguration)
        .build();
  }
}
