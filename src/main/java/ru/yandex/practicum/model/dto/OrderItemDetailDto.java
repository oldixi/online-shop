package ru.yandex.practicum.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class OrderItemDetailDto {
    private Long orderId;
    private Long id;
    private String title;
    private String description;
    private String imagePath;
    private int count;
    @Builder.Default
    private BigDecimal price = BigDecimal.valueOf(0);
    @Builder.Default
    private BigDecimal totalSum = BigDecimal.valueOf(0);
}
