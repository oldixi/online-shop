package ru.yandex.practicum.model.dto;

import lombok.*;
import org.springframework.http.codec.multipart.FilePart;
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
    private FilePart image;
    @Builder.Default
    private BigDecimal price = BigDecimal.valueOf(0);
}
