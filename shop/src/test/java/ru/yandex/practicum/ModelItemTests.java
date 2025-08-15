package ru.yandex.practicum;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mapper.ItemMapper;
import ru.yandex.practicum.model.dto.ItemCreateDto;
import ru.yandex.practicum.model.dto.ItemDto;
import ru.yandex.practicum.model.entity.Item;
import ru.yandex.practicum.repository.ItemRepository;
import ru.yandex.practicum.service.ItemInCacheService;
import ru.yandex.practicum.service.ItemInCartService;
import ru.yandex.practicum.service.ItemService;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class ModelItemTests {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private ItemInCartService itemInCartService;
    @Mock
    private ItemInCacheService cacheService;

    @InjectMocks
    private ItemService itemService;

    @Value("${shop.image.path}")
    private String imagePath;

    @Test
    void testAddItem() {
        ItemCreateDto itemCreateDto = ItemCreateDto.builder()
                .title("Товар 1")
                .description("Товар для mock проверки")
                .price(BigDecimal.valueOf(10,50))
                .build();

        Item item = Item.builder()
                .title("Товар 1")
                .description("Товар для mock проверки")
                .price(BigDecimal.valueOf(10))
                .build();
        try {
            MultipartFile picture = new MockMultipartFile("shop.png",
                    Files.readAllBytes(new File("shop.png").toPath()));
            item.setImage(picture.getBytes());
        } catch (IOException ignore) {}

        ItemDto itemDto = ItemDto.builder()
                .title("Товар 1")
                .description("Товар для mock проверки")
                .price(BigDecimal.valueOf(10))
                .imagePath(imagePath + "1L")
                .build();

        when(itemMapper.toItem(any(ItemCreateDto.class))).thenReturn(item);
        when(itemRepository.save(any(Item.class))).thenReturn(Mono.just(item));
        when(itemMapper.toDto(any(Item.class))).thenReturn(itemDto);
        itemService.saveItem(Mono.just(itemCreateDto))
                .doOnNext(itemRes -> assertThat(itemRes).isEqualTo(itemDto))
                .subscribe();
    }

    @Test
    void testGetImage() {
        Item item = Item.builder()
                .id(1L)
                .title("Товар 1")
                .description("Товар для mock проверки")
                .price(BigDecimal.valueOf(10))
                .build();
        try {
            MultipartFile picture = new MockMultipartFile("shop.png",
                    Files.readAllBytes(new File("shop.png").toPath()));
            item.setImage(picture.getBytes());
        } catch (IOException ignore) {}

        when(cacheService.getImage(any(Long.class))).thenReturn(Mono.just(item.getImage()));
        itemService.getImage(1L)
                .doOnNext(image -> assertThat(image).isEqualTo(item.getImage()))
                .subscribe();
        verify(cacheService).getImage(1L);
    }

    @Test
    void testGetItemDtoById() {
        ItemDto itemDto = ItemDto.builder()
                .title("Товар 1")
                .description("Товар для mock проверки")
                .price(BigDecimal.valueOf(10))
                .imagePath(imagePath + "1L")
                .build();

        ItemDto item = ItemDto.builder()
                .id(1L)
                .title("Товар 1")
                .description("Товар для mock проверки")
                .price(BigDecimal.valueOf(10))
                .imagePath(imagePath + "1L")
                .build();

        when(cacheService.getItemDtoById(any(Long.class))).thenReturn(Mono.just(item));
        when(itemMapper.toDto(any(Item.class))).thenReturn(itemDto);
        when(itemInCartService.getCountByItemIdAndLogin(any(Long.class), anyString())).thenReturn(Mono.just(0));
        itemService.getItemDtoById(1L, "user")
                .doOnNext(itemRes -> assertThat(item).isEqualTo(itemRes))
                .subscribe();
    }
}
