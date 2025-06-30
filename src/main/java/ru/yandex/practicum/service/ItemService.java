package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.enumiration.ECartAction;
import ru.yandex.practicum.enumiration.ESort;
import ru.yandex.practicum.mapper.ItemMapper;
import ru.yandex.practicum.model.dto.*;
import ru.yandex.practicum.model.entity.Item;
import ru.yandex.practicum.repository.ItemRepository;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.List;
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

    public ItemsWithPagingDto getItems(String search, String sort, int pageNumber, int pageSize) {
        Pageable page = switch(ESort.valueOf(sort.toUpperCase())) {
            case NO -> PageRequest.of(pageNumber - 1, pageSize);
            case ALPHA -> PageRequest.of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.ASC, "title"));
            case PRICE -> PageRequest.of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.ASC, "price"));
        };

        Page<Item> items;
        if (search != null && !search.isBlank())
            items = itemRepository.getItemsByTitleLike(search, page);
        else
            items = itemRepository.findAll(page);
        List<ItemDto> itemsDto = itemMapper.toListDto(items);
        itemsDto.forEach(item -> item.setCount(cartService.getItemCountInCart(item.getId())));

        PagingParametersDto pagingParametersDto = PagingParametersDto.builder()
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .hasPrevious(pageNumber > 1)
                .hasNext(pageNumber < Math.ceilDiv(itemRepository.count(), pageSize))
                .build();

        AtomicInteger index = new AtomicInteger();
        List<List<ItemDto>> itemsDtoPartitions = itemsDto.stream()
                .collect(Collectors.groupingBy(it -> index.getAndIncrement() / itemsRowCount))
                .values()
                .stream()
                .toList();
        return new ItemsWithPagingDto(itemsDtoPartitions, pagingParametersDto);
    }

    @Transactional
    public ItemDto saveItem(ItemCreateDto item) {
        return itemMapper.toDto(itemRepository.save(itemMapper.toItem(item)));
    }

    public ItemDto getItemDtoById(Long id) {
        ItemDto item = itemMapper.toDto(itemRepository.findById(id).orElse(new Item()));
        item.setCount(cartService.getItemCountInCart(id));
        return item;
    }

    public byte[] getImage(Long id) {
        return itemRepository.findById(id).orElse(new Item()).getImage();
    }

    public void actionWithItemInCart(Long itemId, String action) {
        log.debug("Start actionWithItemInCart: itemId={}, action={}", itemId, action);
        Map<Long, ItemDto> itemsInCart = cartService.getItemsInCart();
        log.trace("Processing actionWithItemInCart: itemsInCart={}", itemsInCart);
        ItemDto itemInCart;
        if (itemsInCart.containsKey(itemId)) itemInCart = itemsInCart.get(itemId);
        else itemInCart = getItemDtoById(itemId);

        switch (ECartAction.valueOf(action.toUpperCase())) {
            case PLUS -> itemInCart.setCount(itemInCart.getCount() + 1);
            case MINUS -> {
                if (itemInCart.getCount() >= 1) itemInCart.setCount(itemInCart.getCount() - 1);
            }
            case DELETE -> itemInCart.setCount(0);
        }

        log.trace("Process actionWithItemInCart, itemInCart={}, itemsInCart={}", itemInCart, itemsInCart);
        if (itemInCart.getCount() == 0) itemsInCart.remove(itemId);
        else itemsInCart.put(itemId, itemInCart);

        cartService.refresh(itemsInCart, itemInCart);
    }
}
