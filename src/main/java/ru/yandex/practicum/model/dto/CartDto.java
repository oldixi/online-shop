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
    private Map<Long, ItemDto> items = new HashMap<>();
    private BigDecimal total = BigDecimal.valueOf(0);
    private boolean empty = true;
}
