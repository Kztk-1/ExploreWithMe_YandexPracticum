package ru.yandex.practicum.explore_with_me.feature.participationrequest.service;

import ru.yandex.practicum.explore_with_me.feature.participationrequest.dto.UserRequestDto;
import ru.yandex.practicum.explore_with_me.feature.participationrequest.model.UserRequest;

import java.util.List;

public interface UserRequestService {

    List<UserRequestDto> getUserRequests(Long userId);

    UserRequestDto addUserRequest(Long userId, Long eventId);

    UserRequestDto cancelRequest(Long userId, Long requestId);
}
