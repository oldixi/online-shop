package ru.yandex.practicum.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.model.dto.ItemDto;
import ru.yandex.practicum.model.entity.ItemInOrder;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ItemInOrderMapper {
    private final ModelMapper mapper;

    public ItemInOrder toItemInOrder(ItemDto item) {
        log.debug("Start toItemInOrder: itemDto={}", item);
        ItemInOrder itemInOrder = mapper.map(item, ItemInOrder.class);
        itemInOrder.setId(null);
        itemInOrder.setItemId(item.getId());
        log.trace("Finish toItemInOrder: itemInOrder={}", itemInOrder);
        return itemInOrder;
    }

    public ItemDto toDto(ItemInOrder item) {
        log.debug("Start toItemInOrder: itemDto={}", item);
        ItemDto itemInOrder = mapper.map(item, ItemDto.class);
        itemInOrder.setId(item.getItemId());
        log.trace("Finish toItemInOrder: itemInOrder={}", itemInOrder);
        return itemInOrder;
    }

    public Flux<ItemDto> toItemInOrderListFromFlux(Flux<ItemInOrder> entities) {
        return entities.map(this::toDto);
    }

    public List<ItemDto> toItemDtoList(List<ItemInOrder> entities) {
        return entities.stream()
                .map(this::toDto)
                .toList();
    }

    public List<ItemInOrder> toItemInOrderList(List<ItemDto> entities) {
        return entities.stream().map(this::toItemInOrder).toList();
    }
}
