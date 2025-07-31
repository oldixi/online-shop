package ru.yandex.practicum.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import ru.yandex.practicum.model.dto.CartDto;
import ru.yandex.practicum.model.dto.ItemDto;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Configuration
@Slf4j
public class WebConfiguration {
    @Bean()
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean("cart")
    public CartDto cart() {
        log.info("Initialize cart");
        return new CartDto();
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer itemCacheCustomizer() {
        return builder -> builder.withCacheConfiguration(
                "item",
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.of(5, ChronoUnit.SECONDS))
                        .serializeValuesWith(RedisSerializationContext
                                .SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<>(ItemDto.class))));
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer itemsCacheCustomizer() {
        return builder -> builder.withCacheConfiguration(
                "items",
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.of(5, ChronoUnit.SECONDS))
                        .serializeValuesWith(RedisSerializationContext
                                .SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<>(List.class))));
    }
}
