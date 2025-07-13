package ru.yandex.practicum.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ItemsWithPagingDto {
    private List<List<ItemDto>> items;
    private PagingParametersDto paging;
}
