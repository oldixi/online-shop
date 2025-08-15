package ru.yandex.practicum.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.enumiration.EUserRole;
import ru.yandex.practicum.model.dto.NewUserDto;
import ru.yandex.practicum.model.dto.RoleDto;
import ru.yandex.practicum.model.dto.UserDto;
import ru.yandex.practicum.model.entity.User;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserMapper {
    private final ModelMapper mapper;

    public UserDto toUserDto(User user) {
        UserDto userDto = mapper.map(user, UserDto.class);
        log.info("Start toUserDto: user={}, userDto={}", user, userDto);
        userDto.setAuthorities(Arrays.stream(user.getRoles().toUpperCase().split(",|, "))
                .map(role -> {
                    EUserRole roleStr = EUserRole.UNAUTHORIZED;
                    try {
                        roleStr = EUserRole.valueOf(role);
                    } catch (Exception ignore) {}
                    return new RoleDto(roleStr);
                }).toList());
        userDto.getAuthorities().forEach(auth -> log.info("Finish toUserDto: auth={}", auth.getAuthority()));
        return userDto;
    }

    public User toUser(NewUserDto userDto) {
        return mapper.map(userDto, User.class);
    }
}
