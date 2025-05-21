package ru.yandex.practicum.explore_with_me.feature.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.explore_with_me.feature.event.dto.EventFullDto;
import ru.yandex.practicum.explore_with_me.feature.event.dto.UpdateEventAdminRequest;
import ru.yandex.practicum.explore_with_me.feature.event.model.EventState;
import ru.yandex.practicum.explore_with_me.feature.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final EventService eventService;

    /**
     * Получение событий с фильтрами администратора
     */
    @GetMapping
    public ResponseEntity<List<EventFullDto>> getEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<EventState> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(from / size, size);
        List<EventFullDto> events = eventService.getEventsAdmin(
                users, states, categories, rangeStart, rangeEnd, pageable);
        return ResponseEntity.ok(events);
    }

    /**
     * Обновление события администратором
     */
    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateEventAdmin(
            @PathVariable Long eventId,
            @RequestBody UpdateEventAdminRequest updateRequest) {
        EventFullDto updated = eventService.updateEventAdmin(eventId, updateRequest);
        return ResponseEntity.ok(updated);
    }
}
