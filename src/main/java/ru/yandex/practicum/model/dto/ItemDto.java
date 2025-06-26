package ru.yandex.practicum.model.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ItemDto {
    private Long id;
    private String title;
    private String description;
    private String imagePath;
    private int count;
    private BigDecimal price = BigDecimal.valueOf(0);
}
