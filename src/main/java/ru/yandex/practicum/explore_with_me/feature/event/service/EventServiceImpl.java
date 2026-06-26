package ru.yandex.practicum.explore_with_me.feature.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.explore_with_me.config.Config;
import ru.yandex.practicum.explore_with_me.exception.ConflictException;
import ru.yandex.practicum.explore_with_me.exception.NotFoundException;
import ru.yandex.practicum.explore_with_me.feature.event.dto.*;
import ru.yandex.practicum.explore_with_me.feature.event.mapper.EventMapper;
import ru.yandex.practicum.explore_with_me.feature.event.model.Event;
import ru.yandex.practicum.explore_with_me.feature.event.model.EventState;
import ru.yandex.practicum.explore_with_me.feature.event.model.SortType;
import ru.yandex.practicum.explore_with_me.feature.event.repository.EventRepository;
import ru.yandex.practicum.explore_with_me.feature.participationrequest.dto.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.explore_with_me.feature.participationrequest.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.explore_with_me.feature.participationrequest.dto.UserRequestDto;
import ru.yandex.practicum.explore_with_me.feature.participationrequest.mapper.UserRequestMapper;
import ru.yandex.practicum.explore_with_me.feature.participationrequest.model.RequestStatus;
import ru.yandex.practicum.explore_with_me.feature.participationrequest.model.UserRequest;
import ru.yandex.practicum.explore_with_me.feature.participationrequest.repository.UserRequestRepository;
import ru.yandex.practicum.explore_with_me.feature.user.model.User;
import ru.yandex.practicum.explore_with_me.feature.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Primary
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final UserRequestRepository requestRepository;
    private final UserRequestMapper requestMapper;

    @Override
    public List<EventFullDto> getEventsAdmin(List<Long> users, List<EventState> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable) {
        List<Event> events = eventRepository.findAllByAdminFilters(users, states, categories, rangeStart, rangeEnd, pageable)
                .toList();
        return events.stream()
                .map(eventMapper::toFullDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(eventId));

        eventMapper.updateFromAdminRequest(updateRequest, event);
        switch (updateRequest.getStateAction()) {
            case PUBLISH_EVENT:
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException();
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
                break;
            case REJECT_EVENT:
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException(Config.EVENT_PUBLISH_CONFLICT_EXCEPTION_MESSAGE);
                }
                event.setState(EventState.CANCELED);
                break;
        }

        Event updated = eventRepository.save(event);
        return eventMapper.toFullDto(updated);
    }

    @Override
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(eventId));

        if (event.getState() != EventState.PENDING) {
            throw new ConflictException();
        }
        eventMapper.updateFromUserRequest(updateRequest, event);

        Event updated = eventRepository.save(event);
        return eventMapper.toFullDto(updated);
    }

    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, SortType sort, Pageable pageable) {
        if (sort != null) {
            Sort sortObj;
            switch (sort) {
                case VIEWS -> sortObj = Sort.by(Sort.Direction.DESC, "views");
                case EVENT_DATE -> sortObj = Sort.by(Sort.Direction.ASC, "eventDate");
                default -> sortObj = Sort.by(Sort.Direction.ASC, "eventDate");
            }
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortObj);
        }

        return eventRepository.findPublic(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, pageable)
                .stream()
                .map(eventMapper::toShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventShortDto getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format(Config.NOT_FOUND_EXCEPTION, eventId)));
        return eventMapper.toShortDto(event);
    }

    @Override
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        Event event = eventMapper.fromNewEventDto(newEventDto);
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(userId));
        event.setInitiator(initiator);

        Event saved = eventRepository.save(event);
        return eventMapper.toFullDto(saved);
    }

    @Override
    public List<EventShortDto> getEventsByUserId(Long userId, Pageable pageable) {
        Page<Event> page = eventRepository.findByInitiatorId(userId, pageable);
        return page.getContent().stream()
                .map(eventMapper::toShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getUserEventById(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(eventId));
        return eventMapper.toFullDto(event);
    }

    @Override
    public List<UserRequestDto> getEventRequests(Long userId, Long eventId) {
        // Убеждаемся, что событие принадлежит пользователю
        eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(eventId));

        return requestRepository.findAllByEventIdAndEventInitiatorId(eventId, userId).stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequests(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(eventId));

        List<UserRequest> requests = requestRepository.findAllByEventIdAndIdIn(eventId, updateRequest.getRequestIds());

        boolean hasNonPending = requests.stream().anyMatch(r -> r.getStatus() != RequestStatus.PENDING);
        if (hasNonPending) {
            throw new ConflictException(Config.REQUEST_NOT_PENDING_EXCEPTION_MESSAGE);
        }

        List<UserRequest> confirmed = new ArrayList<>();
        List<UserRequest> rejected = new ArrayList<>();

        if (updateRequest.getStatus() == RequestStatus.REJECTED) {
            requests.forEach(r -> r.setStatus(RequestStatus.REJECTED));
            rejected.addAll(requests);
        } else if (updateRequest.getStatus() == RequestStatus.CONFIRMED) {
            int limit = event.getParticipantLimit() == null ? 0 : event.getParticipantLimit();
            int alreadyConfirmed = event.getConfirmedRequests() == null ? 0 : event.getConfirmedRequests();
            int available = limit == 0 ? Integer.MAX_VALUE : limit - alreadyConfirmed;

            if (available <= 0) {
                throw new ConflictException(Config.REQUEST_PARTICIPANT_LIMIT_EXCEPTION_MESSAGE);
            }

            for (UserRequest req : requests) {
                if (available > 0) {
                    req.setStatus(RequestStatus.CONFIRMED);
                    confirmed.add(req);
                    available--;
                } else {
                    req.setStatus(RequestStatus.REJECTED);
                    rejected.add(req);
                }
            }

            event.setConfirmedRequests(alreadyConfirmed + confirmed.size());
            eventRepository.save(event);
        }

        requestRepository.saveAll(requests);

        return new EventRequestStatusUpdateResult(
                confirmed.stream().map(requestMapper::toDto).collect(Collectors.toList()),
                rejected.stream().map(requestMapper::toDto).collect(Collectors.toList())
        );
    }
}
