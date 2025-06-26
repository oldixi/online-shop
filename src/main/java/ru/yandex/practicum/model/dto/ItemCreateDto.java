package ru.yandex.practicum.model.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ItemCreateDto {
    private Long id;
    private String title;
    private String description;
    private MultipartFile image;
    private BigDecimal price = BigDecimal.valueOf(0);
}
