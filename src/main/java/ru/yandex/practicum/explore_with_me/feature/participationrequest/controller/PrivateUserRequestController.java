package ru.yandex.practicum.explore_with_me.feature.participationrequest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.explore_with_me.feature.participationrequest.dto.UserRequestDto;
import ru.yandex.practicum.explore_with_me.feature.participationrequest.mapper.UserRequestMapper;
import ru.yandex.practicum.explore_with_me.feature.participationrequest.service.UserRequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
public class PrivateUserRequestController {

    private final UserRequestService service;
    private final UserRequestMapper mapper;

    @GetMapping
    public List<UserRequestDto> getUserRequests(@PathVariable Long userId) {
        return service.getUserRequests(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserRequestDto addUserParticipationRequest(
            @PathVariable Long userId,
            @RequestParam Long eventId
    ) {
        return service.addUserRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public UserRequestDto cancelRequest(
            @PathVariable Long userId,
            @PathVariable Long requestId
    ) {
        return service.cancelRequest(userId, requestId);
    }
}

