package ru.yandex.practicum.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class OrderDto {
    private Long id;
    private List<ItemDto> items;
    @Builder.Default
    private BigDecimal totalSum  = BigDecimal.valueOf(0);
}
