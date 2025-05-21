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
    private final UserService userService;
    private final UserRepository userRepository;

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
    public List<EventFullDto> getEventsByUserId(Long userId) {
        List<Event> result = eventRepository.findAllByInit(userId); //Доделаю
        return result.stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());
    }


}
