package ru.yandex.practicum.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.model.dto.ItemDto;
import ru.yandex.practicum.model.dto.ItemInCartDto;
import ru.yandex.practicum.model.entity.ItemInCart;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ItemInCartMapper {
    private final ModelMapper mapper;

    public ItemDto toItemDto(ItemInCartDto itemInCartDto) {
        log.info("Start toItemDto: itemInCartDto={}", itemInCartDto);
        Long itemId = itemInCartDto.getItemId();
        ItemDto itemDto = mapper.map(itemInCartDto, ItemDto.class);
        itemDto.setId(itemId);
        return itemDto;
    }

    public ItemDto toItemDto(ItemInCart item) {
        log.info("Start toItemDto: item={}", item);
        Long itemId = item.getItemId();
        ItemDto itemDto = mapper.map(item, ItemDto.class);
        itemDto.setId(itemId);
        return itemDto;
    }

    public List<ItemDto> toItemDto(List<ItemInCartDto> items) {
        log.info("Start toItemDto: items={}", items);
        return items.stream().map(this::toItemDto).toList();
    }

    public ItemInCartDto toItemInCartDto(ItemInCart item) {
        log.info("Start toItemInCartDto: item={}", item);
        return mapper.map(item, ItemInCartDto.class);
    }

    public ItemInCart toItemInCart(ItemDto itemDto) {
        log.info("Start toItemInCartDto: itemDto={}", itemDto);
        ItemInCart itemInCart = mapper.map(itemDto, ItemInCart.class);
        itemInCart.setId(null);
        itemInCart.setItemId(itemDto.getId());
        log.info("Finish toItemInCartDto: itemInCartDto={}", itemInCart);
        return itemInCart;
    }
}
