package ru.yandex.practicum.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CartDto {
    @Builder.Default
    private Map<Long, ItemDto> items = new HashMap<>();
    @Builder.Default
    private BigDecimal total = BigDecimal.valueOf(0);
    @Builder.Default
    private boolean empty = true;
}
