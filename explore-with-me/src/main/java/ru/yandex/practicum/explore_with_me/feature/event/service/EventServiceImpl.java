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

    @Override
    public EventFullDto createEvent(Long userId, NewEventDto eventDto) {
        Event event = eventMapper.toEntity(eventDto);
    public EventFullDto getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));
        return eventMapper.toFullDto(event);
    }

        // 1) initiator
        User initiator = userService.getUser(userId).orElseThrow();
        event.setInitiator(initiator);
        // 2) state
        event.setState(EventState.PENDING);
        // 3) category
        Category category = categoryRepository.getReferenceById(eventDto.getCategory());
        event.setCategory(category);

        Event savedEvent = eventRepository.save(event);
        return eventMapper.toDto(savedEvent);
    }

    @Override
    public List<EventFullDto> getEventsByUserId(Long userId) {
        List<Event> result = eventRepository.findAllByInit(userId); //Доделаю
        return result.stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());
    }


}
