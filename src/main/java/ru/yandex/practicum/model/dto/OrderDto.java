package ru.yandex.practicum.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class OrderDto implements Comparable<OrderDto> {
    private Long id;
    private List<ItemDto> items;
    private BigDecimal totalSum;

    @Override
    public int compareTo(OrderDto o) {
        return Long.compare(this.id, o.id) * -1;
    }
}
