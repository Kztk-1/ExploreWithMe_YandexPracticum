package ru.yandex.practicum.explore_with_me.feature.event.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.explore_with_me.feature.event.dto.EventFullDto;
import ru.yandex.practicum.explore_with_me.feature.event.dto.NewEventDto;
import ru.yandex.practicum.explore_with_me.feature.event.service.EventService;
import ru.yandex.practicum.explore_with_me.feature.event.service.EventServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")  // Базовый путь для действий пользователя
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    EventFullDto createEvent(@PathVariable Long userId, @RequestBody NewEventDto eventDto) {
        return eventService.createEvent(userId, eventDto);
    }

    @GetMapping
    List<EventFullDto> getEventsByUserId(@PathVariable Long userId) {
        return eventService.getEventsByUserId(userId);
    }

}
