package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.model.entity.ItemInOrder;
import ru.yandex.practicum.repository.ItemInOrderRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemInOrderService {
    private final ItemInOrderRepository itemInOrderRepository;

    public Flux<ItemInOrder> saveItemsInOrder(List<ItemInOrder> items) {
        return itemInOrderRepository.saveAll(items);
    }

    public Flux<ItemInOrder> getItemInOrderByOrderId(Long orderId) {
        return itemInOrderRepository.getItemInOrderByOrderId(orderId);
    }

    public Flux<ItemInOrder> getItems() {
        return itemInOrderRepository.findAll();
    }
}
