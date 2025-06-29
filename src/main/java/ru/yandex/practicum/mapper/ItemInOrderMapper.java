package ru.yandex.practicum.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.model.dto.ItemDto;
import ru.yandex.practicum.model.dto.OrderItemDetailDto;
import ru.yandex.practicum.model.entity.ItemInOrder;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ItemInOrderMapper {
    private final ModelMapper mapper;

    public ItemInOrder toItemInOrder(ItemDto item) {
        log.info("Start toItemInOrder: itemDto={}", item);
        ItemInOrder itemInOrder = mapper.map(item, ItemInOrder.class);
        itemInOrder.setId(null);
        itemInOrder.setItemId(item.getId());
        log.info("Start toItemInOrder: itemInOrder={}", itemInOrder);
        return itemInOrder;
    }

    public List<ItemInOrder> toItemInOrderList(List<ItemDto> entities) {
        return entities.stream().map(this::toItemInOrder).toList();
    }

    public ItemDto toItemInOrder(OrderItemDetailDto item) {
        return mapper.map(item, ItemDto.class);
    }

    public List<ItemDto> toItemDtoList(List<OrderItemDetailDto> entities) {
        return entities.stream().map(this::toItemInOrder).toList();
    }
}
