package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
/*    @Query("select o from Order o left outer join fetch o.items where o.id = :id")
    Optional<Order> getOrderById(@Param("id") Long id);

    @Query("select o from Order o left outer join fetch o.items")
    List<Order> getOrders();*/
}
