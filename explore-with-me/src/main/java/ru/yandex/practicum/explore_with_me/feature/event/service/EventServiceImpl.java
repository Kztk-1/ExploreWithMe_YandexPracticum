package ru.yandex.practicum.explore_with_me.feature.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.explore_with_me.feature.category.model.Category;
import ru.yandex.practicum.explore_with_me.feature.category.repository.CategoryRepository;
import ru.yandex.practicum.explore_with_me.feature.event.dto.EventFullDto;
import ru.yandex.practicum.explore_with_me.feature.event.dto.NewEventDto;
import ru.yandex.practicum.explore_with_me.feature.event.mapper.EventMapper;
import ru.yandex.practicum.explore_with_me.feature.event.model.Event;
import ru.yandex.practicum.explore_with_me.feature.event.model.EventState;
import ru.yandex.practicum.explore_with_me.feature.event.model.SortType;
import ru.yandex.practicum.explore_with_me.feature.event.repository.EventRepository;
import ru.yandex.practicum.explore_with_me.feature.user.model.User;
import ru.yandex.practicum.explore_with_me.feature.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Primary
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;

    @Override
    public List<EventFullDto> getEventsAdmin(List<Long> users, List<EventState> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable) {
        // TODO: build specification or criteria query based on filters
        List<Event> events = findAllByAdminFilters(users, states, categories, rangeStart, rangeEnd, pageable);
        return events.stream()
                .map(eventMapper::toFullDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

        eventMapper.updateFromAdminRequest(updateRequest, event);
        Event updated = eventRepository.save(event);
        return eventMapper.toFullDto(updated);
    }

    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Pageable pageable) {
        // TODO: implement filtering, availability and sorting
        List<Event> events = eventRepository.findPublic(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, pageable);
        return events.stream()
                .map(eventMapper::toShortDto)
                .collect(Collectors.toList());
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, SortType sort, Pageable pageable) {
    }

    @Override
    public EventFullDto getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));
        return eventMapper.toFullDto(event);
    }

    @Override
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        Event event = eventMapper.fromNewEventDto(newEventDto);
        User initiator = userRepository.getReferenceById(userId);

        event.setInitiator(initiator);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);

        Event saved = eventRepository.save(event);
        return eventMapper.toFullDto(saved);
    }

    @Override
    public List<EventShortDto> getEventsByUserId(Long userId, Pageable pageable) {
        Page<Event> page = eventRepository.findByInitiatorId(userId, pageable);
        List<Event> events = page.getContent();
        return events.stream()
                .map(eventMapper::toShortDto)
                .collect(Collectors.toList());
    }


}
