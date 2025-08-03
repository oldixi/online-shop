package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.enumiration.ESort;
import ru.yandex.practicum.mapper.ItemMapper;
import ru.yandex.practicum.model.dto.ItemDto;
import ru.yandex.practicum.model.entity.Item;
import ru.yandex.practicum.repository.ItemRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemInCacheService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Value("${shop.items.row:5}")
    int itemsRowCount;

    @Cacheable(cacheNames = "items", key="{#search, #sort, #pageNumber, #pageSize}")
    public Mono<List<ItemDto>>/*Mono<List<List<ItemDto>>>*/ getItems(String search, String sort, int pageNumber, int pageSize) {
        log.info("Start getItems: pageNumber={}, pageSize={}, sort={}, search={}", pageNumber, pageSize, sort, search);
        Pageable page = switch(ESort.valueOf(sort.toUpperCase())) {
            case NO -> PageRequest.of(pageNumber - 1, pageSize);
            case ALPHA -> PageRequest.of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.ASC, "title"));
            case PRICE -> PageRequest.of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.ASC, "price"));
        };
        Flux<Item> items;
        if (search != null && !search.isBlank())
            items = itemRepository.getItemsByTitleLike(search, page);
        else
            items = itemRepository.findBy(page);
        return itemMapper.toListDto(items).log().collectList();
    }

    @Cacheable(value = "item", key = "#id")
    public Mono<ItemDto> getItemDtoById(Long id) {
        log.info("Start getItemDtoById: id={}", id);
        return itemRepository.findById(id)
                .map(itemMapper::toDto);
    }

    @Cacheable(value = "picture", key = "#id")
    public Mono<byte[]> getImage(Long id) {
        return itemRepository.findById(id).map(Item::getImage).onErrorComplete();
    }
}
