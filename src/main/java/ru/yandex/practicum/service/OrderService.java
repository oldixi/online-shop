package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mapper.ItemInOrderMapper;
import ru.yandex.practicum.mapper.OrderMapper;
import ru.yandex.practicum.model.dto.CartDto;

import ru.yandex.practicum.model.dto.ItemDto;
import ru.yandex.practicum.model.dto.OrderDto;
import ru.yandex.practicum.model.entity.ItemInOrder;
import ru.yandex.practicum.model.entity.Order;
import ru.yandex.practicum.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;

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
        CartDto cartCopy = cartService.getCart();
        log.info("Start buy: cartCopy={}", cartCopy);
        if (!cartCopy.isEmpty()) {
            OrderDto orderDto = OrderDto.builder()
                    .totalSum(cartCopy.getTotal())
                    .items(cartCopy.getItems().values().stream().toList())
                    .build();
            List<ItemInOrder> items = itemInOrderMapper.toItemInOrderList(cartCopy.getItems().values().stream().toList());
            log.trace("Processing buy: items={}", items);
            Mono<Long> id = orderRepository.save(orderMapper.toOrder(orderDto))
                    .map(Order::getId)
                    .log()
                    .doOnNext(orderId -> {
                        items.forEach(item -> item.setOrderId(orderId));
                        log.info("Processing buy: items in order {}", items);
                        itemInOrderService.saveItemsInOrder(items).subscribe();
                    });
            cartService.clearCart();
            return id;
        } else return Mono.just(0L);
    }

    public Mono<OrderDto> getOrderById(Long orderId) {
        Flux<ItemDto> items = itemInOrderMapper
                .toItemInOrderListFromFlux(itemInOrderService.getItemInOrderByOrderId(orderId));
        Mono<BigDecimal> sum = items
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return orderRepository.findById(orderId)
                .map(orderMapper::toOrder)
                .zipWith(items.collectList(), (order, itemsInOrder) -> {
                    order.setItems(itemsInOrder);
                    return order;
                })
                .zipWith(sum, (order, sumOfOrder) -> {
                    order.setTotalSum(sumOfOrder);
                    return order;
                })
                .log();
    }

    public Flux<OrderDto> getOrders() {
        log.info("Enter getOrders");
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
                .log();
    }
}
