package ru.yandex.practicum.explore_with_me.feature.event.service;

import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.explore_with_me.feature.event.dto.*;
import ru.yandex.practicum.explore_with_me.feature.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    /**
     * Админ: получение событий по фильтрам и пагинации
     */
    List<EventFullDto> getEventsAdmin(
            List<Long> users,
            List<EventState> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Pageable pageable
    );

    /**
     * Админ: обновление события
     */
    EventFullDto updateEventAdmin(
            Long eventId,
            UpdateEventAdminRequest updateRequest
    );

    /**
     * Публично: поиск событий по фильтрам и пагинации
     */
    List<EventShortDto> getPublicEvents(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            SortType sort,
            Pageable pageable
    );

    /**
     * Публично: получение события по ID
     */
    EventShortDto getEventById(
            Long eventId
    );

    /**
     * Пользователь: создание нового события
     */
    EventFullDto createEvent(
            Long userId,
            NewEventDto newEventDto
    );

    /**
     * Пользователь: получение списка своих событий с пагинацией
     */
    List<EventShortDto> getEventsByUserId(
            Long userId,
            Pageable pageable
    );

    /**
     * Пользователь: получение своего события по ID
     */
    EventFullDto getUserEventById(
            Long userId,
            Long eventId
    );

    /**
     * Пользователь: обновление своего события
     */
    EventFullDto updateEventByUser(
            Long userId,
            Long eventId,
            UpdateEventUserRequest updateRequest
    );
}
