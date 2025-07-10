package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.model.entity.ItemInOrder;

public interface ItemInOrderRepository extends JpaRepository<ItemInOrder, Long> {
}
