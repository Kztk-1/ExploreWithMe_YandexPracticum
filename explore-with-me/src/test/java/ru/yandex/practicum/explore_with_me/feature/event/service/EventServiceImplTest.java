package ru.yandex.practicum.explore_with_me.feature.event.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.explore_with_me.feature.category.repository.CategoryRepository;
import ru.yandex.practicum.explore_with_me.feature.event.dto.EventFullDto;
import ru.yandex.practicum.explore_with_me.feature.event.dto.NewEventDto;
import ru.yandex.practicum.explore_with_me.feature.event.model.Event;
import ru.yandex.practicum.explore_with_me.feature.event.model.EventState;
import ru.yandex.practicum.explore_with_me.feature.event.model.Location;
import ru.yandex.practicum.explore_with_me.feature.event.repository.EventRepository;
import ru.yandex.practicum.explore_with_me.feature.user.dto.NewUserRequest;
import ru.yandex.practicum.explore_with_me.feature.user.dto.UserDto;
import ru.yandex.practicum.explore_with_me.feature.user.model.User;
import ru.yandex.practicum.explore_with_me.feature.user.repository.UserRepository;
import ru.yandex.practicum.explore_with_me.feature.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional // Откатывает транзакции после каждого теста
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = {"/clear-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class EventServiceImplTest {

    private final EventService eventService;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final CategoryRepository categoryRepository;

    @Test
    void createEvent_shouldCreateEvent() {
        //1
        UserDto initiator = userService.createUser(new NewUserRequest("Name", "email@gmail.com"));
        String title = "", annotation = "annotation", description = "description";
        LocalDateTime eventDate = LocalDateTime.now().plusDays(3);
        Location location = new Location(56.332F, 78.098F);
        boolean paid = false, requestModeration = true;
        int participantLimit = 10;
        int category = 2;

        NewEventDto newEventDto = NewEventDto.builder()
                .title(title)
                .annotation(annotation)
                .description(description)
                .eventDate(eventDate)
                .location(location)
                .paid(paid)
                .category(category)
                .participantLimit(participantLimit)
                .requestModeration(requestModeration)
                .build();
        //2
        EventFullDto eventDto = eventService.createEvent(initiator.getId(), newEventDto);

        //3
        assertThat(eventDto.getId()).isNotNull();
        assertThat(eventDto.getTitle()).isEqualTo(title);
        assertThat(eventDto.getDescription()).isEqualTo(description);
        assertThat(eventDto.getAnnotation()).isEqualTo(annotation);
        assertThat(eventDto.getEventDate()).isEqualTo(eventDate);
        assertThat(eventDto.getLocation()).isEqualTo(location);
        assertThat(eventDto.getPaid()).isEqualTo(paid);
        assertThat(eventDto.getRequestModeration()).isEqualTo(requestModeration);
        assertThat(eventDto.getParticipantLimit()).isEqualTo(participantLimit);
        assertThat(eventDto.getCategory().getId()).isEqualTo(category);
        //3.1
        assertThat(eventRepository.findById(eventDto.getId())).isPresent();
    }

    @Test
    void getEventsByUserId_shouldReturnAllEventsByUserId() {
        UserDto initiator = userService.createUser(NewUserRequest.builder()
                .name("Name")
                .email("email@gmail.com")
                .build());

        String title1 = "Title One", annotation1 = "Annotation for event one", description1 = "Description for event one";
        String title2 = "Title Two", annotation2 = "Annotation for event two", description2 = "Description for event two";
        LocalDateTime eventDate1 = LocalDateTime.now().plusDays(3);
        LocalDateTime eventDate2 = LocalDateTime.now().plusDays(5);
        Location location1 = new Location(56.332F, 78.098F);
        Location location2 = new Location(55.123F, 37.456F);
        boolean paid = false, requestModeration = true;
        int participantLimit = 10, category = 2;

        NewEventDto newEventDto1 = NewEventDto.builder()
                .title(title1)
                .annotation(annotation1)
                .description(description1)
                .eventDate(eventDate1)
                .location(location1)
                .paid(paid)
                .category(category)
                .participantLimit(participantLimit)
                .requestModeration(requestModeration)
                .build();

        NewEventDto newEventDto2 = NewEventDto.builder()
                .title(title2)
                .annotation(annotation2)
                .description(description2)
                .eventDate(eventDate2)
                .location(location2)
                .paid(paid)
                .category(category)
                .participantLimit(participantLimit)
                .requestModeration(requestModeration)
                .build();

        EventFullDto event1 = eventService.createEvent(initiator.getId(), newEventDto1);
        EventFullDto event2 = eventService.createEvent(initiator.getId(), newEventDto2);

        List<EventFullDto> events = eventService.getEventsByUserId(initiator.getId());

        assertThat(events).hasSize(2);
        assertThat(events)
                .extracting(EventFullDto::getId)
                .containsExactlyInAnyOrder(event1.getId(), event2.getId());
    }

}
