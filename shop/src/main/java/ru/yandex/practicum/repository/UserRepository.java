package ru.yandex.practicum.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.entity.User;

public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    Mono<User> findUserByLoginIgnoreCase(String login);
}
