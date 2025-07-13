package ru.yandex.practicum.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.dto.*;
import ru.yandex.practicum.model.entity.Item;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class ItemMapper {
    private final ModelMapper mapper;

    @Value("${shop.image.path}")
    private String imagePath;

    public ItemDto toDto(Item item) {
        ItemDto dto = mapper.map(item, ItemDto.class);
        dto.setImagePath(imagePath + dto.getId());
        return dto;
    }

    public Flux<ItemDto> toListDto(Flux<Item> entities) {
        return entities
                .map(this::toDto);
    }

    public Item toItem(ItemCreateDto dto) {
        Item item = mapper.map(dto, Item.class);
        if (item != null) {
            try {
                if (dto.getImage() != null && !dto.getImage().isEmpty())
                    item.setImage(dto.getImage().getBytes());
            } catch (IOException e) {
                return item;
            }
        }
        return item;
    }
}
