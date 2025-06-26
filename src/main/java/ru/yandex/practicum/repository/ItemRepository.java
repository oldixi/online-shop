package ru.yandex.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.entity.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Page<Item> getItemsByTitleLike(String search, Pageable page);

/*    @Modifying
    @Query(value = "update posts set title = :title, text = :text, tags = :tags where id = :id ", nativeQuery = true)
    void editByIdWithoutImage(@Param("id") Long id, @Param("title") String title, @Param("text") String text, @Param("tags") String tags);

    @Modifying
    @Query(value = "update posts set likes_count = :likesCount where id = :id ", nativeQuery = true)
    void likeById(@Param("id") Long id, @Param("likesCount") int likesCount);*/
}
