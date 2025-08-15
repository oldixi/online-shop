package ru.yandex.practicum.model.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class NewUserDto {
    private String login;
    private String password;
    private String roles;
}
