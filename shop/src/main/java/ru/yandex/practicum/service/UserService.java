package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.enumiration.EUserRole;
import ru.yandex.practicum.exception.UserAlreadyExistsException;
import ru.yandex.practicum.mapper.UserMapper;
import ru.yandex.practicum.model.dto.NewUserDto;
import ru.yandex.practicum.model.dto.UserDto;
import ru.yandex.practicum.repository.UserRepository;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class UserService implements ReactiveUserDetailsService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findUserByLoginIgnoreCase(username)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new UsernameNotFoundException("User Not Found"))))
                .map(userMapper::toUserDto);
    }

    public Mono<String> addUser(Mono<NewUserDto> user) {
        return user.flatMap(newUser -> userRepository.findUserByLoginIgnoreCase(newUser.getLogin())
                .flatMap(userDto -> Mono.error(new UserAlreadyExistsException(userDto.getLogin())))
                .log()
                .switchIfEmpty(Mono.defer(() -> user.flatMap(createdUser -> {
                    createdUser.setPassword(encoder.encode(createdUser.getPassword()));
                    createdUser.setRoles(EUserRole.ROLE_USER.name());
                    return userRepository.save(userMapper.toUser(createdUser));
                }).log())))
                .thenReturn(user.map(createdUser -> userRepository.findUserByLoginIgnoreCase(createdUser.getLogin())))
                .flatMap(Function.identity()).flatMap(Function.identity())
                .map(userMapper::toUserDto)
                .map(UserDto::getLogin);
    }
}
