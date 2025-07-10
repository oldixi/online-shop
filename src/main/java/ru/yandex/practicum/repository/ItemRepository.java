package ru.yandex.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.model.entity.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Page<Item> getItemsByTitleLike(String search, Pageable page);
}
