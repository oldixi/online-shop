package ru.yandex.practicum.model.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ItemInCartDto {
    private Long id;
    private String title;
    private int count;
    private BigDecimal price;
    private String description;
    private String imagePath;
    private String login;
    private Long itemId;
}
