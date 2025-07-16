package ru.yandex.practicum.model.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Table("items")
public class Item {
    @Id
    private Long id;
    @ToString.Exclude
    private byte[] image;
    private String title;
    private String description;
    private BigDecimal price;
}
