package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.model.entity.Order;
public interface OrderRepository extends JpaRepository<Order, Long> {
}
