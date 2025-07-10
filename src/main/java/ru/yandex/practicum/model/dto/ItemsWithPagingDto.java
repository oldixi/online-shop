package ru.yandex.practicum.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class ItemsWithPagingDto {
    List<List<ItemDto>> items;
    private PagingParametersDto paging;
}
