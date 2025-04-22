package ru.yandex.practicum.explore_with_me.feature.user.service;


import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.explore_with_me.feature.user.dto.NewUserRequest;
import ru.yandex.practicum.explore_with_me.feature.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(NewUserRequest newUserRequest);
    List<UserDto> getUsers(List<Long> ids, Pageable pageable);
    void deleteUser(Long userId);
}