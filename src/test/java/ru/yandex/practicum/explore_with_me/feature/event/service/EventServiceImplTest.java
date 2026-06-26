package ru.yandex.practicum.explore_with_me.feature.event.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import ru.yandex.practicum.explore_with_me.config.Config;
import ru.yandex.practicum.explore_with_me.exception.ConflictException;
import ru.yandex.practicum.explore_with_me.exception.NotFoundException;
import ru.yandex.practicum.explore_with_me.feature.category.model.Category;
import ru.yandex.practicum.explore_with_me.feature.event.dto.*;
import ru.yandex.practicum.explore_with_me.feature.event.mapper.EventMapper;
import ru.yandex.practicum.explore_with_me.feature.event.model.*;
import ru.yandex.practicum.explore_with_me.feature.event.repository.EventRepository;
import ru.yandex.practicum.explore_with_me.feature.user.model.User;
import ru.yandex.practicum.explore_with_me.feature.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    private final User testUser = User.builder()
            .id(1L)
            .name("Test User")
            .email("test@example.com")
            .build();

    private final Event testEvent = Event.builder()
            .id(1L)
            .title("Test Event")
            .annotation("Test Annotation")
            .description("Test Description")
            .eventDate(LocalDateTime.now().plusDays(1))
            .location(new Location(52.5200F, 13.4050F))
            .category(new Category(1, "Test Category"))
            .initiator(testUser)
            .state(EventState.PENDING)
            .createdOn(LocalDateTime.now())
            .paid(false)
            .participantLimit(10)
            .requestModeration(true)
            .confirmedRequests(5)
            .views(100L)
            .build();

    @Test
    void getEventsAdmin_shouldReturnFilteredEvents() {
        // given
        List<Long> users = List.of(1L);
        List<EventState> states = List.of(EventState.PENDING);
        List<Long> categories = List.of(1L);
        LocalDateTime rangeStart = LocalDateTime.now();
        LocalDateTime rangeEnd = LocalDateTime.now().plusDays(2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> eventPage = new PageImpl<>(List.of(testEvent));

        EventFullDto expectedDto = new EventFullDto();
        when(eventRepository.findAllByAdminFilters(users, states, categories, rangeStart, rangeEnd, pageable))
                .thenReturn(eventPage);
        when(eventMapper.toFullDto(testEvent)).thenReturn(expectedDto);

        // when
        List<EventFullDto> result = eventService.getEventsAdmin(
                users, states, categories, rangeStart, rangeEnd, pageable);

        // then
        assertEquals(1, result.size());
        assertSame(expectedDto, result.get(0));
        verify(eventRepository).findAllByAdminFilters(users, states, categories, rangeStart, rangeEnd, pageable);
        verify(eventMapper).toFullDto(testEvent);
    }

    @Test
    void updateEventAdmin_shouldPublishEvent() {
        // given
        UpdateEventAdminRequest updateRequest = new UpdateEventAdminRequest();
        updateRequest.setStateAction(AdminStateAction.PUBLISH_EVENT);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
        when(eventMapper.toFullDto(testEvent)).thenReturn(new EventFullDto());

        // when
        EventFullDto result = eventService.updateEventAdmin(1L, updateRequest);

        // then
        assertNotNull(result);
        assertEquals(EventState.PUBLISHED, testEvent.getState());
        assertNotNull(testEvent.getPublishedOn());
        verify(eventRepository).save(testEvent);
    }

    @Test
    void updateEventAdmin_shouldRejectEvent() {
        // given
        UpdateEventAdminRequest updateRequest = new UpdateEventAdminRequest();
        updateRequest.setStateAction(AdminStateAction.REJECT_EVENT);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
        when(eventMapper.toFullDto(testEvent)).thenReturn(new EventFullDto());

        // when
        EventFullDto result = eventService.updateEventAdmin(1L, updateRequest);

        // then
        assertNotNull(result);
        assertEquals(EventState.CANCELED, testEvent.getState());
        verify(eventRepository).save(testEvent);
    }

    @Test
    void updateEventAdmin_shouldThrowConflictWhenPublishPublishedEvent() {
        // given
        testEvent.setState(EventState.PUBLISHED);
        UpdateEventAdminRequest updateRequest = new UpdateEventAdminRequest();
        updateRequest.setStateAction(AdminStateAction.PUBLISH_EVENT);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        // when & then
        assertThrows(ConflictException.class, () ->
                eventService.updateEventAdmin(1L, updateRequest));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateEventAdmin_shouldThrowConflictWhenRejectPublishedEvent() {
        // given
        testEvent.setState(EventState.PUBLISHED);
        UpdateEventAdminRequest updateRequest = new UpdateEventAdminRequest();
        updateRequest.setStateAction(AdminStateAction.REJECT_EVENT);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        // when & then
        ConflictException exception = assertThrows(ConflictException.class, () ->
                eventService.updateEventAdmin(1L, updateRequest));
        assertEquals(Config.EVENT_PUBLISH_CONFLICT_EXCEPTION_MESSAGE, exception.getMessage());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateEventByUser_shouldUpdateEvent() {
        // given
        UpdateEventUserRequest updateRequest = new UpdateEventUserRequest();
        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
        when(eventMapper.toFullDto(testEvent)).thenReturn(new EventFullDto());

        // when
        EventFullDto result = eventService.updateEventByUser(1L, 1L, updateRequest);

        // then
        assertNotNull(result);
        verify(eventMapper).updateFromUserRequest(updateRequest, testEvent);
        verify(eventRepository).save(testEvent);
    }

    @Test
    void updateEventByUser_shouldThrowConflictWhenNotPending() {
        // given
        testEvent.setState(EventState.PUBLISHED);
        UpdateEventUserRequest updateRequest = new UpdateEventUserRequest();
        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(testEvent));

        // when & then
        assertThrows(ConflictException.class, () ->
                eventService.updateEventByUser(1L, 1L, updateRequest));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void getPublicEvents_shouldReturnEventsWithSortByViews() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> eventPage = new PageImpl<>(List.of(testEvent));

        when(eventRepository.findPublic(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(eventPage);
        when(eventMapper.toShortDto(testEvent)).thenReturn(new EventShortDto());

        // when
        List<EventShortDto> result = eventService.getPublicEvents(
                "text", List.of(1L), true,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                false, SortType.VIEWS, pageable);

        // then
        assertEquals(1, result.size());
        /*
        verify(eventRepository).findPublic(
                "text", List.of(1L), true,
                any(LocalDateTime.class), any(LocalDateTime.class),
                false,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "views")));
         */
        /*
        verify(eventRepository).findPublic(
                "text",                      // raw
                List.of(1L),                 // raw
                true,                        // raw
                any(LocalDateTime.class),    // matcher
                any(LocalDateTime.class),    // matcher
                false,                       // raw
                PageRequest.of(0, 10, Sort.by(...) ) // raw
        );
         */

        verify(eventRepository).findPublic(
                eq("text"),
                eq(List.of(1L)),
                eq(true),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(false),
                eq(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "views")))
        );

    }

    @Test
    void getPublicEvents_shouldReturnOnlyAvailableEvents() {
        // given
        Event availableEvent = testEvent;
        availableEvent.setParticipantLimit(10);
        availableEvent.setConfirmedRequests(5);

        Event unavailableEvent = testEvent.toBuilder()
                .id(2L)
                .participantLimit(5)
                .confirmedRequests(5)
                .build();

        Page<Event> eventPage = new PageImpl<>(List.of(availableEvent, unavailableEvent));

        when(eventRepository.findPublic(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(eventPage);
        when(eventMapper.toShortDto(availableEvent)).thenReturn(new EventShortDto());

        // when
        List<EventShortDto> result = eventService.getPublicEvents(
                null, null, null,
                null, null,
                true, null, PageRequest.of(0, 10));

        // then
        assertEquals(1, result.size());
        verify(eventMapper, times(1)).toShortDto(any());
    }

    @Test
    void getEventById_shouldReturnEventShortDto() {
        // given
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventMapper.toShortDto(testEvent)).thenReturn(new EventShortDto());

        // when
        EventShortDto result = eventService.getEventById(1L);

        // then
        assertNotNull(result);
        verify(eventRepository).findById(1L);
        verify(eventMapper).toShortDto(testEvent);
    }

    @Test
    void getEventById_shouldThrowNotFoundException() {
        // given
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> eventService.getEventById(1L));
    }

    @Test
    void createEvent_shouldSaveAndReturnEvent() {
        // given
        NewEventDto newEventDto = new NewEventDto();
        when(userRepository.getReferenceById(1L)).thenReturn(testUser);
        when(eventMapper.fromNewEventDto(newEventDto)).thenReturn(testEvent);
        when(eventRepository.save(testEvent)).thenReturn(testEvent);
        when(eventMapper.toFullDto(testEvent)).thenReturn(new EventFullDto());

        // when
        EventFullDto result = eventService.createEvent(1L, newEventDto);

        // then
        assertNotNull(result);
        assertEquals(EventState.PENDING, testEvent.getState());
        assertNotNull(testEvent.getCreatedOn());
        assertEquals(testUser, testEvent.getInitiator());
        verify(eventRepository).save(testEvent);
    }

    @Test
    void getEventsByUserId_shouldReturnUserEvents() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> eventPage = new PageImpl<>(List.of(testEvent));
        when(eventRepository.findByInitiatorId(1L, pageable)).thenReturn(eventPage);
        when(eventMapper.toShortDto(testEvent)).thenReturn(new EventShortDto());

        // when
        List<EventShortDto> result = eventService.getEventsByUserId(1L, pageable);

        // then
        assertEquals(1, result.size());
        verify(eventRepository).findByInitiatorId(1L, pageable);
        verify(eventMapper).toShortDto(testEvent);
    }

    @Test
    void getUserEventById_shouldReturnEventFullDto() {
        // given
        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(testEvent));
        when(eventMapper.toFullDto(testEvent)).thenReturn(new EventFullDto());

        // when
        EventFullDto result = eventService.getUserEventById(1L, 1L);

        // then
        assertNotNull(result);
        verify(eventRepository).findByIdAndInitiatorId(1L, 1L);
        verify(eventMapper).toFullDto(testEvent);
    }

    @Test
    void getUserEventById_shouldThrowNotFoundException() {
        // given
        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> eventService.getUserEventById(1L, 1L));
    }
}