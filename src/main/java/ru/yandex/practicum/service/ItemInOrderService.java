package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.mapper.ItemInOrderMapper;
import ru.yandex.practicum.model.entity.ItemInOrder;
import ru.yandex.practicum.repository.ItemInOrderRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemInOrderService {
    private final ItemInOrderRepository itemInOrderRepository;
    private final ItemInOrderMapper itemInOrderMapper;

    public void saveItemsInOrder(List<ItemInOrder> items) {
        itemInOrderRepository.saveAll(items);
    }
}
