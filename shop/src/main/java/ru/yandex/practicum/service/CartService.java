package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
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

    private final PaymentsService paymentsService;

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

    public Mono<CartDto> refresh(Map<Long, ItemDto> itemsInCart, ItemDto itemInCart) {
        log.info("Start refresh: itemsInCart={}, itemInCart={}", itemsInCart, itemInCart);
        return Mono.zip(Mono.just(new CartDto()).map(
                cart -> {
                    cart.setItems(itemsInCart);
                    cart.setEmpty(itemsInCart.size() == 0);
                    return cart;
                }).log(), paymentsService.getBalance().log(), getTotalPrice().log())
                .flatMap(data -> {
                    if (itemsInCart.size() > 0) data.getT1().setTotal(data.getT3().add(itemInCart.getPrice()));
                    else data.getT1().setTotal(BigDecimal.valueOf(0));
                    data.getT1().setCanBuy(data.getT2().signum() > 0);
                    return cartRepository.update(data.getT1());
                })
                .log();
    }
}
