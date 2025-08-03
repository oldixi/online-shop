package ru.yandex.practicum.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.codegen.ru.yandex.practicum.ApiClient;
import main.codegen.ru.yandex.practicum.api.DefaultApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import ru.yandex.practicum.model.dto.CartDto;
import ru.yandex.practicum.model.dto.ItemDto;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class WebConfiguration {
    @Value("${payments.server.url}")
    private String apiPath;

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
    public RedisCacheManagerBuilderCustomizer itemPictureCacheCustomizer() {
        return builder -> builder.withCacheConfiguration(
                "picture",
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.of(1, ChronoUnit.DAYS))
                        .serializeValuesWith(RedisSerializationContext
                                .SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<>(byte[].class))));
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer itemsCacheCustomizer(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
        var om = jacksonObjectMapperBuilder.createXmlMapper(false).build();
        return builder -> builder.withCacheConfiguration(
                "items",
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.of(5, ChronoUnit.SECONDS))
                        .serializeValuesWith(RedisSerializationContext
                                .SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer(om.getTypeFactory()
                                        .constructCollectionType(List.class, ItemDto.class)))));
    }

    @Bean
    public ApiClient apiClient(DefaultApi defaultApi) {
        defaultApi.getApiClient().setBasePath(apiPath);
        log.info("apiClientBasePath={}", defaultApi.getApiClient().getBasePath());
        return defaultApi.getApiClient();
    }
}
