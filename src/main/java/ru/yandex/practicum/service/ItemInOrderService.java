package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.entity.ItemInOrder;
import ru.yandex.practicum.repository.ItemInOrderRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemInOrderService {
    private final ItemInOrderRepository itemInOrderRepository;

    public Mono<ItemInOrder> save(ItemInOrder item) {
        log.debug("Start save: item={}", item);
        return itemInOrderRepository.save(item);
    }

    public Flux<ItemInOrder> getItemInOrderByOrderId(Long orderId) {
        return itemInOrderRepository.getItemInOrderByOrderId(orderId);
    }

    public Flux<ItemInOrder> getItems() {
        return itemInOrderRepository.findAll();
    }
}
