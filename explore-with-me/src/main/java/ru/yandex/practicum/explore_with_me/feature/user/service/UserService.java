package ru.yandex.practicum.explore_with_me.feature.user.service;


import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.explore_with_me.feature.user.dto.NewUserRequest;
import ru.yandex.practicum.explore_with_me.feature.user.dto.UserDto;
import ru.yandex.practicum.explore_with_me.feature.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDto createUser(NewUserRequest newUserRequest);
    List<UserDto> getUsers(List<Long> ids, Pageable pageable);
    Optional<User> getUser(Long id);
    void deleteUser(Long userId);
}