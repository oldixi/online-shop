package ru.yandex.practicum.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.model.dto.OrderDto;
import ru.yandex.practicum.model.entity.Order;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderMapper {
    private final ModelMapper mapper;

    public Order toOrder(OrderDto orderDto) {
        log.debug("Start toOrder: orderDto={}, order={}", orderDto, mapper.map(orderDto, Order.class));
        return mapper.map(orderDto, Order.class);
    }
}
