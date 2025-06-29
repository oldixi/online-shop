package ru.yandex.practicum.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.model.dto.CartDto;
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
}
