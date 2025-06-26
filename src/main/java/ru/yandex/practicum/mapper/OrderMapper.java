package ru.yandex.practicum.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.model.dto.OrderDto;
import ru.yandex.practicum.model.entity.Order;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderMapper {
    private final ModelMapper mapper;

    public OrderDto toOrderDto(Order comment) {
        return mapper.map(comment, OrderDto.class);
    }

    public List<OrderDto> toOrdersDto(List<Order> orders) {
        return orders.stream().map(this::toOrderDto).toList();
    }

    public Order toOrder(OrderDto orderDto) {
        log.info("Start toOrder: orderDto={}, order={}", orderDto, mapper.map(orderDto, Order.class));
        return mapper.map(orderDto, Order.class);
    }
}
