package ru.yandex.practicum.explore_with_me.feature.event.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.explore_with_me.feature.event.dto.EventFullDto;
import ru.yandex.practicum.explore_with_me.feature.event.dto.EventShortDto;
import ru.yandex.practicum.explore_with_me.feature.event.dto.NewEventDto;
import ru.yandex.practicum.explore_with_me.feature.event.dto.UpdateEventUserRequest;
import ru.yandex.practicum.explore_with_me.feature.event.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class PrivateEventController {
    /*
     `POST /users/{userId}/events` → `createEvent()`
  - `GET /users/{userId}/events` → `getUserEvents()`
  - `GET /users/{userId}/events/{eventId}` → `getUserEventById()`
  - `PATCH /users/{userId}/events/{eventId}` → `updateEventByUser()
     */

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventFullDto> createEvent(
            @PathVariable Long userId,
            @RequestBody NewEventDto newEventDto) {
        EventFullDto created = eventService.createEvent(userId, newEventDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getUserEvents(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") @Min(0) int from,
            @RequestParam(defaultValue = "10") @Min(1) int size) {

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
        List<EventShortDto> events = eventService.getEventsByUserId(userId, pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getUserEventById(
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        EventFullDto event = eventService.getUserEventById(userId, eventId);
        return ResponseEntity.ok(event);
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateEventByUser(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody UpdateEventUserRequest updateRequest) {
        EventFullDto updated = eventService.updateEventByUser(userId, eventId, updateRequest);
        return ResponseEntity.ok(updated);
    }


}
