package ru.yandex.practicum.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.yandex.practicum.model.entity.Order;
public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
}
