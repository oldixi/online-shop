package ru.yandex.practicum.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.entity.ItemInCart;

public interface ItemInCartRepository extends R2dbcRepository<ItemInCart, Long> {
    Mono<Void> deleteByLoginIgnoreCase(String login);

    Mono<Void> deleteByItemIdAndLoginIgnoreCase(Long itemId, String login);

    Mono<ItemInCart> getByItemIdAndLoginIgnoreCase(Long itemId, String login);

    Flux<ItemInCart> getByLoginIgnoreCase(String login);
}
