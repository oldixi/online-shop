package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.entity.ItemInCart;
import ru.yandex.practicum.repository.ItemInCartRepository;

import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemInCartService {
    private final ItemInCartRepository itemInCartRepository;

    @CacheEvict(cacheNames = "itemsInCart", key="#login")
    public Mono<Void> deleteByLogin(String login) {
        log.info("Start deleteByLogin: login={}", login);
        return itemInCartRepository.deleteByLoginIgnoreCase(login).then();
    }

    public Mono<ItemInCart> getByItemIdAndLogin(Long itemId, String login) {
        log.info("Start getByItemIdAndLogin: itemId={}, login={}", itemId, login);
        return itemInCartRepository.getByItemIdAndLoginIgnoreCase(itemId, login).log();
    }

    public Mono<Integer> getCountByItemIdAndLogin(Long itemId, String login) {
        log.info("Start getByItemIdAndLogin: itemId={}, login={}", itemId, login);
        return itemInCartRepository.getByItemIdAndLoginIgnoreCase(itemId, login).map(ItemInCart::getCount);
    }

    @Cacheable(cacheNames = "itemsInCart", key="#login")
    public Mono<List<ItemInCart>> getByLogin(String login) {
        return itemInCartRepository.getByLoginIgnoreCase(login).collectList();
    }

    @CacheEvict(cacheNames = "itemsInCart", key="#login")
    public Mono<ItemInCart> changeItemCountInCart(ItemInCart itemInCart, String action, String login) {
        log.info("Start changeItemCountInCart: login={}, itemDto={}, action={}", login, itemInCart, action);
        return Mono.just(itemInCart)
                .map(itemInCartRepository::save)
                .flatMap(Function.identity());
    }

    @CacheEvict(cacheNames = "itemsInCart", key="#login")
    public Mono<Void> removeItemFromCart(Long itemId, String login) {
        log.info("Start deleteByItemIdAndLogin: itemId={}, login={}", itemId, login);
        return itemInCartRepository.deleteByItemIdAndLoginIgnoreCase(itemId, login)
                .log();
    }
}
