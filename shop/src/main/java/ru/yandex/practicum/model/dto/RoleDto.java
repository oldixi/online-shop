package ru.yandex.practicum.model.dto;

import org.springframework.security.core.GrantedAuthority;
import ru.yandex.practicum.enumiration.EUserRole;

public class RoleDto implements GrantedAuthority {
    private final EUserRole role;

    public RoleDto(EUserRole role) {
        this.role = role;
    }

    @Override
    public String getAuthority() {
        return role.name();
    }
}
