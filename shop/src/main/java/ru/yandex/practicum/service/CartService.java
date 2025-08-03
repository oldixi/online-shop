package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.enumiration.ECartAction;
import ru.yandex.practicum.model.dto.CartDto;
import ru.yandex.practicum.model.dto.ItemDto;
import ru.yandex.practicum.repository.CartRepositoryImpl;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {
    private final CartRepositoryImpl cartRepository;

    public Mono<Void> clearCart() {
        return cartRepository.delete();
    }

    public Mono<CartDto> getCart() {
        return cartRepository.findAll().log();
    }

    public Mono<Integer> getItemCountInCart(Long itemId) {
        log.info("Start getItemCountInCart: itemId={}", itemId);
        return cartRepository.getItemCountInCart(itemId).log();
    }

    public Mono<BigDecimal> getTotalPrice() {
        log.info("Start getTotalPrice");
        return cartRepository.getTotalPrice().log();
    }

    public Mono<Map<Long, ItemDto>> getItemsInCart() {
        log.info("Start getItemsInCart");
        return cartRepository.getItemsInCart().log();
    }

    public Mono<CartDto> refresh(ItemDto itemDto, String action) {
        log.info("Start refresh: itemDto={}, action={}", itemDto, action);
        switch (ECartAction.valueOf(action.toUpperCase())) {
            case PLUS -> itemDto.setCount(itemDto.getCount() + 1);
            case MINUS -> {
                if (itemDto.getCount() >= 1) itemDto.setCount(itemDto.getCount() - 1);
            }
            case DELETE -> itemDto.setCount(0);
        }
        if (itemDto.getCount() == 0) return cartRepository.removeItemFromCart(itemDto);
        return cartRepository.changeItemCountInCart(itemDto, action);
    }
}
