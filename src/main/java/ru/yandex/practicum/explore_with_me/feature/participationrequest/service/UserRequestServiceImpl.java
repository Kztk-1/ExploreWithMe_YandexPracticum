package ru.yandex.practicum.explore_with_me.feature.participationrequest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.explore_with_me.config.Config;
import ru.yandex.practicum.explore_with_me.exception.ConflictException;
import ru.yandex.practicum.explore_with_me.exception.NotFoundException;
import ru.yandex.practicum.explore_with_me.feature.event.model.Event;
import ru.yandex.practicum.explore_with_me.feature.event.model.EventState;
import ru.yandex.practicum.explore_with_me.feature.event.repository.EventRepository;
import ru.yandex.practicum.explore_with_me.feature.participationrequest.dto.UserRequestDto;
import ru.yandex.practicum.explore_with_me.feature.participationrequest.mapper.UserRequestMapper;
import ru.yandex.practicum.explore_with_me.feature.participationrequest.model.RequestStatus;
import ru.yandex.practicum.explore_with_me.feature.participationrequest.model.UserRequest;
import ru.yandex.practicum.explore_with_me.feature.participationrequest.repository.UserRequestRepository;
import ru.yandex.practicum.explore_with_me.feature.user.model.User;
import ru.yandex.practicum.explore_with_me.feature.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@Primary
@RequiredArgsConstructor
public class UserRequestServiceImpl implements UserRequestService {

    private final UserRequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    private final UserRequestMapper requestMapper;

    @Override
    public List<UserRequestDto> getUserRequests(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(userId);
        }

        return requestRepository.findAllByRequesterId(userId).stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Override
    public UserRequestDto addUserRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(userId));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(eventId));

        // нельзя добавить повторный запрос (Ожидается код ошибки 409)
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException(Config.REQUEST_DUPLICATE_EXCEPTION_MESSAGE);
        }
        // инициатор события не может добавить запрос на участие в своём событии (Ожидается код ошибки 409)
        if (user.getId() == event.getInitiator().getId()) {
            throw new ConflictException(Config.REQUEST_OWN_EVENT_EXCEPTION_MESSAGE);
        }
        // нельзя участвовать в неопубликованном событии (Ожидается код ошибки 409)
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException(Config.REQUEST_UNPUBLISHED_EVENT_EXCEPTION_MESSAGE);
        }

        // если у события достигнут лимит запросов на участие - необходимо вернуть ошибку (Ожидается код ошибки 409)
        if (event.getParticipantLimit() > 0
                && event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConflictException(Config.REQUEST_PARTICIPANT_LIMIT_EXCEPTION_MESSAGE);
        }


        // Проверки пройдены, собираем Request
        RequestStatus status = !event.getRequestModeration()
                ? RequestStatus.CONFIRMED // нет модерации - сразу принимаем
                : RequestStatus.PENDING;
        UserRequest newRequest = UserRequest.builder()
                .requester(user)
                .event(event)
                .status(status)
                .build();

        UserRequest saved = requestRepository.save(newRequest);
        return requestMapper.toDto(saved);
    }

    @Override
    public UserRequestDto cancelRequest(Long userId, Long requestId) {
        UserRequest request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException(requestId));

        request.setStatus(RequestStatus.CANCELED);
        UserRequest saved = requestRepository.save(request);

        return requestMapper.toDto(saved);
    }
}
