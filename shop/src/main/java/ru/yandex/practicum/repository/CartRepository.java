package ru.yandex.practicum.repository;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.dto.CartDto;
import ru.yandex.practicum.model.dto.ItemDto;

import java.math.BigDecimal;
import java.util.Map;

public interface CartRepository {
    Mono<Void> delete();

    Mono<CartDto> findAll();

    Mono<Integer> getItemCountInCart(Long itemId);

    Mono<Map<Long, ItemDto>> getItemsInCart();

    Mono<BigDecimal> getTotalPrice();

    Mono<CartDto> update(CartDto cartDto);
}
