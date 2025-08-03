package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.yandex.practicum.mapper.ItemMapper;
import ru.yandex.practicum.model.dto.*;
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
    private final ItemInCacheService cacheService;

    @Value("${shop.items.row:5}")
    int itemsRowCount;

    public Mono<List<List<ItemDto>>> getItems(String search, String sort, int pageNumber, int pageSize) {
        AtomicInteger index = new AtomicInteger();
        return cacheService.getItems(search, sort, pageNumber, pageSize)
                .flatMapIterable(itemsList -> itemsList)
                .flatMap(itemDto -> cartService.getItemCountInCart(itemDto.getId())
                        .zipWith(Mono.just(itemDto), (count, item) -> {
                            itemDto.setCount(count);
                            return itemDto;
                        }))
                .collectList()
                .map(itemsListWithCount -> itemsListWithCount.stream()
                        .collect(Collectors.groupingBy(it -> index.getAndIncrement() / itemsRowCount))
                        .values()
                        .stream()
                        .toList());
    }

    public Mono<PagingParametersDto> getPaging(String search, String sort, int pageNumber, int pageSize) {
        log.debug("Start getPaging: pageNumber={}, pageSize={}", pageNumber, pageSize);
        return Mono.just(PagingParametersDto.builder()
                        .pageNumber(pageNumber)
                        .pageSize(pageSize)
                        .hasPrevious(pageNumber > 1)
                        .build())
                        .zipWith(cacheService.getItems(search, sort, pageNumber, pageSize)
                                .flatMapIterable(itemsList -> itemsList).count()
                                .map(cnt -> pageNumber < Math.ceilDiv(cnt, pageSize)), (paging, isNextPage) -> {
                            paging.setHasNext(isNextPage);
                            return paging;
                        });
    }

    public Mono<ItemDto> getItemDtoById(Long id) {
        return cacheService.getItemDtoById(id)
                .zipWith(cartService.getItemCountInCart(id), (itemDto, count) -> {
                    itemDto.setCount(count);
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
        return cacheService.getImage(id);
    }

    public Mono<ItemDto> actionWithItemInCart(Long itemId, String action) {
        log.debug("Start actionWithItemInCart: itemId={}, action={}", itemId, action);
        Mono<Map<Long, ItemDto>> itemsInCart = cartService.getItemsInCart().log();
        return itemsInCart.flatMap(itemsInCartNow ->
                        itemsInCartNow.containsKey(itemId) ? Mono.just(itemsInCartNow.get(itemId)) : getItemDtoById(itemId))
                .doOnNext(itemDto -> cartService.refresh(itemDto, action))
                .log();
    }
}
