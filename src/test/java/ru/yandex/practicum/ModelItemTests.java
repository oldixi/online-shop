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
import ru.yandex.practicum.service.CartService;
import ru.yandex.practicum.service.ItemService;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.HashMap;

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
    private CartService cartService;

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

        when(itemRepository.findById(any(Long.class))).thenReturn(Mono.just(item));
        itemService.getImage(1L)
                .doOnNext(image -> assertThat(image).isEqualTo(item.getImage()))
                .subscribe();
        verify(itemRepository).findById(1L);
    }

    @Test
    void testGetItemDtoById() {
        ItemDto itemDto = ItemDto.builder()
                .title("Товар 1")
                .description("Товар для mock проверки")
                .price(BigDecimal.valueOf(10))
                .imagePath(imagePath + "1L")
                .build();

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

        when(itemRepository.findById(any(Long.class))).thenReturn(Mono.just(item));
        when(itemMapper.toDto(any(Item.class))).thenReturn(itemDto);
        when(cartService.getItemsInCart()).thenReturn(new HashMap<>());
        itemService.getItemDtoById(1L)
                .doOnNext(itemRes -> assertThat(itemRes).isEqualTo(itemDto))
                .subscribe();
    }
}
