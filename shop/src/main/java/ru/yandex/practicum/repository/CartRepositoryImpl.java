package ru.yandex.practicum.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.dto.CartDto;
import ru.yandex.practicum.model.dto.ItemDto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Repository
public class CartRepositoryImpl implements CartRepository {
    private final CartDto cart;

    @Override
    public Mono<Void> delete() {
        cart.setItems(new HashMap<>());
        cart.setTotal(BigDecimal.valueOf(0));
        cart.setEmpty(true);
        return Mono.just(cart).then();
    }

    @Override
    public Mono<CartDto> findAll() {
        return Mono.just(cart);
    }

    @Override
    public Mono<Integer> getItemCountInCart(Long itemId) {
        if (cart.getItems().containsKey(itemId)) return Mono.just(cart.getItems().get(itemId).getCount());
        return Mono.just(0);
    }

    @Override
    public Mono<Map<Long, ItemDto>> getItemsInCart() {
        return Mono.just(cart.getItems());
    }

    @Override
    public Mono<CartDto> update(CartDto cartDto) {
        cart.setItems(cartDto.getItems());
        cart.setTotal(cartDto.getTotal());
        cart.setEmpty(cartDto.isEmpty());
        return Mono.just(cart);
    }

    @Override
    public Mono<BigDecimal> getTotalPrice() {
        return Mono.just(cart.getTotal());
    }
}
