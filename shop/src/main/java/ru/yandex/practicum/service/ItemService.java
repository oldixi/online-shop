package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.yandex.practicum.enumiration.ECartAction;
import ru.yandex.practicum.enumiration.ESort;
import ru.yandex.practicum.mapper.ItemMapper;
import ru.yandex.practicum.model.dto.*;
import ru.yandex.practicum.model.entity.Item;
import ru.yandex.practicum.repository.ItemRepository;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableRedisRepositories(enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP)

public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final CartService cartService;

    @Value("${shop.items.row:5}")
    int itemsRowCount;

    @Cacheable(cacheNames = "items", key="{#search, #sort, #pageNumber, #pageSize}")
    public Mono<List<List<ItemDto>>> getItems(String search, String sort, int pageNumber, int pageSize) {
        if ("DUMMY".equals(search)) search = null;
        log.info("Start getItems: pageNumber={}, pageSize={}, sort={}, search={}", pageNumber, pageSize, sort, search);
        AtomicInteger index = new AtomicInteger();
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
        return itemMapper.toListDto(items)
                .map(itemDto -> {
                    cartService.getItemCountInCart(itemDto.getId())
                            .zipWith(Mono.just(itemDto), (count, item) -> {
                                itemDto.setCount(count);
                                return itemDto;
                            });
                    return itemDto;
                }).collectList()
                .map(itemsList -> itemsList.stream()
                        .collect(Collectors.groupingBy(it -> index.getAndIncrement() / itemsRowCount))
                        .values()
                        .stream()
                        .toList())
                .log();
    }

    public Mono<PagingParametersDto> getPaging(int pageNumber, int pageSize) {
        log.info("Start getPaging: pageNumber={}, pageSize={}", pageNumber, pageSize);
        return Mono.just(PagingParametersDto.builder()
                        .pageNumber(pageNumber)
                        .pageSize(pageSize)
                        .hasPrevious(pageNumber > 1)
                        .build())
                        .zipWith(itemRepository.count()
                                .map(cnt -> pageNumber < Math.ceilDiv(cnt, pageSize)), (paging, isNextPage) -> {
                            paging.setHasNext(isNextPage);
                            return paging;
                        });
    }

    @Cacheable(value = "item", key = "#id")
    public Mono<ItemDto> getItemDtoById(Long id) {
        log.info("Start getItemDtoById: id={}", id);
        return itemRepository.findById(id)
                .map(itemMapper::toDto)
                .zipWith(cartService.getItemCountInCart(id), (itemDto, amount) -> {
                    itemDto.setCount(amount);
                    return itemDto;
                });
    }

    @Transactional
    public Mono<ItemDto> saveItem(Mono<ItemCreateDto> itemCreatedDto) {
        log.debug("Start saveItem: item={}, thread={}", itemCreatedDto, Thread.currentThread().getName());
        return itemCreatedDto
                .map(dto -> {
                    if (dto.getImage() == null)
                        return itemCreatedDto
                                .map(itemMapper::toItem)
                                .flatMap(itemRepository::save)
                                .log()
                                .map(itemMapper::toDto);
                    return DataBufferUtils.join(dto.getImage().content())
                                .publishOn(Schedulers.boundedElastic())
                                .<byte[]>handle((dataBuffer, sink) -> {
                                    try {
                                        sink.next(dataBuffer.asInputStream().readAllBytes());
                                    } catch (IOException e) {
                                        sink.error(new RuntimeException(e));
                                    }
                                })
                                .zipWith(itemCreatedDto.map(itemMapper::toItem), (byteArray, item) -> {
                                    item.setImage(byteArray);
                                    return item;
                                })
                                .flatMap(itemRepository::save)
                                .map(itemMapper::toDto);
                })
                .flatMap(Function.identity());
    }

    public Mono<byte[]> getImage(Long id) {
        return itemRepository.findById(id).map(Item::getImage).onErrorComplete();
    }

    public Mono<ItemDto> actionWithItemInCart(Long itemId, String action) {
        log.info("Start actionWithItemInCart: itemId={}, action={}", itemId, action);
        Mono<Map<Long, ItemDto>> itemsInCart = cartService.getItemsInCart().log();
        return itemsInCart.flatMap(itemsInCartNow ->  itemsInCartNow.containsKey(itemId) ? Mono.just(itemsInCartNow.get(itemId)) :
                getItemDtoById(itemId))
                .log()
                .zipWith(itemsInCart, (item, cart) -> {
                        switch (ECartAction.valueOf(action.toUpperCase())) {
                            case PLUS -> item.setCount(item.getCount() + 1);
                            case MINUS -> {
                                if (item.getCount() >= 1) item.setCount(item.getCount() - 1);
                            }
                            case DELETE -> item.setCount(0);
                        }
                        if (item.getCount() == 0) cart.remove(itemId);
                        else cart.put(itemId, item);
                        cartService.refresh(cart, item).log();
                        return item;
                    })
                .log();
    }
}
