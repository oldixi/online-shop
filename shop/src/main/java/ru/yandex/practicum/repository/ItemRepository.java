package ru.yandex.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.model.entity.Item;

public interface ItemRepository extends R2dbcRepository<Item, Long> {
    Flux<Item> getItemsByTitleLike(String search, Pageable page);
    Flux<Item> findBy(Pageable page);
}
