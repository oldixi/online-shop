package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.enumiration.ECartAction;
import ru.yandex.practicum.enumiration.ESort;
import ru.yandex.practicum.mapper.ItemMapper;
import ru.yandex.practicum.model.dto.*;
import ru.yandex.practicum.model.entity.Item;
import ru.yandex.practicum.repository.ItemRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final CartService cartService;

    @Value("${shop.items.row:5}")
    int itemsRowCount;

    public Mono<ItemsWithPagingDto> getItems(String search, String sort, int pageNumber, int pageSize) {
        log.info("Start getItems: pageNumber={}, pageSize={}", pageNumber, pageSize);
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

        AtomicInteger index = new AtomicInteger();
        Mono<List<List<ItemDto>>> itemsDto = itemMapper.toListDto(items)
                .map(itemDto -> {
                    itemDto.setCount(cartService.getItemCountInCart(itemDto.getId()));
                    return itemDto;
                }).collectList()
                .map(itemsList -> itemsList.stream()
                    .collect(Collectors.groupingBy(it -> index.getAndIncrement() / itemsRowCount))
                    .values()
                    .stream()
                    .toList());

        var nextPage = itemRepository.count()
                .map(cnt -> pageNumber < Math.ceilDiv(cnt, pageSize));
        Mono<PagingParametersDto> pagingParametersDto = Mono.just(PagingParametersDto.builder()
                        .pageNumber(pageNumber)
                        .pageSize(pageSize)
                        .hasPrevious(pageNumber > 1)
                        .build())
                .zipWith(nextPage, (paging, isNextPage) -> {
                    paging.setHasNext(isNextPage);
                    return paging;
                })
                .log();

        return Mono.just(new ItemsWithPagingDto())
                .zipWith(pagingParametersDto, (res, paging) -> {
                    res.setPaging(paging);
                    return res;
                })
                .zipWith(itemsDto, (res, itemsList) -> {
                    res.setItems(itemsList);
                    return res;
                })
                .log();
    }

    @Transactional
    public Mono<ItemDto> saveItem(ItemCreateDto item) {
        return itemRepository.save(itemMapper.toItem(item))
                .map(itemMapper::toDto);
    }

    public Mono<ItemDto> getItemDtoById(Long id) {
        log.info("Start getItemDtoById: id={}", id);
        return itemRepository.findById(id)
                .map(itemMapper::toDto)
                .map(itemDto -> {
                    itemDto.setCount(cartService.getItemCountInCart(id));
                    return itemDto;
                });
    }

    public Mono<byte[]> getImage(Long id) {
        return itemRepository.findById(id).map(Item::getImage);
    }

    public void actionWithItemInCart(Long itemId, String action) {
        log.info("Start actionWithItemInCart: itemId={}, action={}", itemId, action);
        Map<Long, ItemDto> itemsInCart = cartService.getItemsInCart();
        log.info("Processing actionWithItemInCart: itemsInCart={}", itemsInCart);
        Mono<ItemDto> itemInCart;
        if (itemsInCart.containsKey(itemId)) itemInCart = Mono.just(itemsInCart.get(itemId));
        else itemInCart = getItemDtoById(itemId);

        if (itemInCart != null) {
            itemInCart.map(item -> {
                switch (ECartAction.valueOf(action.toUpperCase())) {
                    case PLUS -> item.setCount(item.getCount() + 1);
                    case MINUS -> {
                        if (item.getCount() >= 1) item.setCount(item.getCount() - 1);
                    }
                    case DELETE -> item.setCount(0);
                }

                log.trace("Process actionWithItemInCart, itemInCart={}, itemsInCart={}", item, itemsInCart);
                if (item.getCount() == 0) itemsInCart.remove(itemId);
                else itemsInCart.put(itemId, item);
                cartService.refresh(itemsInCart, item);
                return item;
            })
                    .subscribe();
        }
        log.info("Finish actionWithItemInCart: itemId={}, action={}", itemId, action);
    }
}
