package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mapper.ItemInOrderMapper;
import ru.yandex.practicum.mapper.OrderMapper;

import ru.yandex.practicum.model.dto.OrderDto;
import ru.yandex.practicum.model.entity.ItemInOrder;
import ru.yandex.practicum.model.entity.Order;
import ru.yandex.practicum.repository.OrderRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CartService cartService;
    private final ItemInOrderService itemInOrderService;
    private final ItemInOrderMapper itemInOrderMapper;

    @Transactional
    public Mono<Long> buy() {
        return orderRepository.save(orderMapper.toOrder(OrderDto.builder()
                        .totalSum(cartService.getCart().getTotal())
                        .items(cartService.getCart().getItems().values().stream().toList())
                        .build()))
                .log()
                .map(Order::getId)
                .log()
                .doOnNext(orderId -> {
                    itemInOrderMapper.toItemInOrderList(cartService.getCart().getItems().values().stream().toList())
                            .forEach(item -> {
                                item.setOrderId(orderId);
                                itemInOrderService.save(item);
                            });
                });
    }

    public Mono<OrderDto> getOrderById(Long orderId) {
        return itemInOrderService.getItemInOrderByOrderId(orderId)
                .groupBy(ItemInOrder::getOrderId)
                .flatMap(Flux::collectList)
                .map(items -> OrderDto.builder()
                        .items(itemInOrderMapper.toItemDtoList(items))
                        .id(items.getFirst().getOrderId())
                        .totalSum(items.stream()
                                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getCount())))
                                .reduce(BigDecimal.ZERO,BigDecimal::add))
                        .build())
                .next();
    }

    public Flux<OrderDto> getOrders() {
        log.debug("Enter getOrders");
        return itemInOrderService.getItems()
                .groupBy(ItemInOrder::getOrderId)
                .flatMap(Flux::collectList)
                .map(items -> OrderDto.builder()
                            .items(itemInOrderMapper.toItemDtoList(items))
                            .id(items.getFirst().getOrderId())
                            .totalSum(items.stream()
                                    .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getCount())))
                                    .reduce(BigDecimal.ZERO,BigDecimal::add))
                            .build())
                .sort();
    }
}
