package ru.yandex.practicum.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.model.entity.ItemInOrder;

public interface ItemInOrderRepository extends ReactiveCrudRepository<ItemInOrder, Long> {
    Flux<ItemInOrder> getItemInOrderByOrderId(Long orderId);
}
