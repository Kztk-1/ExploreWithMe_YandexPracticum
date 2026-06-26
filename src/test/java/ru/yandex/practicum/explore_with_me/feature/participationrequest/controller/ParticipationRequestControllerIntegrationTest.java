package ru.yandex.practicum.explore_with_me.feature.participationrequest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.explore_with_me.BaseIntegrationTest;
import ru.yandex.practicum.explore_with_me.config.Config;
import ru.yandex.practicum.explore_with_me.feature.event.model.EventState;
import ru.yandex.practicum.explore_with_me.feature.event.repository.EventRepository;
import ru.yandex.practicum.explore_with_me.feature.participationrequest.repository.UserRequestRepository;
import ru.yandex.practicum.explore_with_me.feature.participationrequest.service.UserRequestService;
import ru.yandex.practicum.explore_with_me.feature.user.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
//@Sql(scripts = {"/sql/clear-data.sql", "/sql/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ParticipationRequestControllerIntegrationTest extends BaseIntegrationTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final UserRequestRepository requestRepository;
    private final UserRequestService requestService;
    private final EventRepository eventRepository;

    @Test
    void getUserParticipationRequests_shouldReturnUserRequests() throws Exception {
        // Тестовые данные:
        // Пользователь id=2 имеет запросы:
        // - id=1: на событие id=1 (Jazz Night), статус PENDING
        // - id=2: на событие id=3 (Art Exhibition), статус CONFIRMED



        mockMvc.perform(get("/users/{userId}/requests", 2)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[?(@.id==1)].event").value(1))
                .andExpect(jsonPath("$[?(@.id==1)].status").value("PENDING"))
                .andExpect(jsonPath("$[?(@.id==2)].event").value(3))
                .andExpect(jsonPath("$[?(@.id==2)].status").value("CONFIRMED"));
    }

    @Test
    void getUserParticipationRequests_shouldReturnEmptyListForUserWithoutRequests() throws Exception {
        // Пользователь id=4 не имеет запросов на участие
        mockMvc.perform(get("/users/{userId}/requests", 7)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getUserParticipationRequests_shouldReturn404ForNonExistingUser() throws Exception {
        mockMvc.perform(get("/users/{userId}/requests", 999)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(String.format(Config.NOT_FOUND_EXCEPTION, 999)));
    }

    @Test
    void createParticipationRequest_shouldCreatePendingRequestForModeratedEvent() throws Exception {
        // Событие id=1 (Jazz Night) требует модерации, participantLimit=50
        // Пользователь id=3 создает запрос
        mockMvc.perform(post("/users/{userId}/requests", 3)
                        .param("eventId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.event").value(1))
                .andExpect(jsonPath("$.requester").value(3))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.created").isNotEmpty());
    }

    @Test
    void createParticipationRequest_shouldCreateConfirmedRequestForNonModeratedEvent() throws Exception {
        eventRepository.findById(2L).get().setState(EventState.PUBLISHED); // Опубликуем событие

        // Событие id=2 (Marathon) не требует модерации (requestModeration=false)
        // Пользователь id=3 создает запрос, который должен быть автоматически подтвержден
        mockMvc.perform(post("/users/{userId}/requests", 3)
                        .param("eventId", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.event").value(2))
                .andExpect(jsonPath("$.requester").value(3))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void createParticipationRequest_shouldReturn409ForDuplicateRequest() throws Exception {
        // Пользователь id=2 уже имеет запрос на событие id=1
        mockMvc.perform(post("/users/{userId}/requests", 2)
                        .param("eventId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(Config.REQUEST_DUPLICATE_EXCEPTION_MESSAGE));
    }

    @Test
    void createParticipationRequest_shouldReturn409ForOwnEvent() throws Exception {
        // Пользователь id=1 пытается подать заявку на свое собственное событие id=1
        mockMvc.perform(post("/users/{userId}/requests", 1)
                        .param("eventId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(Config.REQUEST_OWN_EVENT_EXCEPTION_MESSAGE));
    }

    @Test
    void createParticipationRequest_shouldReturn409ForUnpublishedEvent() throws Exception {
        // Событие id=5 находится в состоянии PENDING (не опубликовано)
        mockMvc.perform(post("/users/{userId}/requests", 3)
                        .param("eventId", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(Config.REQUEST_UNPUBLISHED_EVENT_EXCEPTION_MESSAGE));
    }

    @Test
    void createParticipationRequest_shouldReturn409WhenParticipantLimitReached() throws Exception {
        var event = eventRepository.findById(4L).get();
        event.setParticipantLimit(1); // TODO пофиксить эту хуйню
        event.setConfirmedRequests(1);


        // Событие id=4 имеет participantLimit=1 и уже 1 подтвержденная заявка
        mockMvc.perform(post("/users/{userId}/requests", 3)
                        .param("eventId", "4")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(Config.REQUEST_PARTICIPANT_LIMIT_EXCEPTION_MESSAGE));
    }

    @Test
    void cancelParticipationRequest_shouldCancelPendingRequest() throws Exception {
        // Запрос id=1 находится в состоянии PENDING, пользователь id=2 отменяет его
        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", 2, 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    void cancelParticipationRequest_shouldCancelConfirmedRequest() throws Exception {
        // Запрос id=2 находится в состоянии CONFIRMED, пользователь id=2 отменяет его
        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", 2, 2)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    void cancelParticipationRequest_shouldReturn404ForNonExistingRequest() throws Exception {
        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", 2, 999)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(String.format(Config.NOT_FOUND_EXCEPTION, 999)));
    }

    @Test
    void cancelParticipationRequest_shouldReturn404ForOtherUsersRequest() throws Exception {
        // Пользователь id=3 пытается отменить запрос id=1, который принадлежит пользователю id=2
        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", 3, 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(String.format(Config.NOT_FOUND_EXCEPTION, 1)));
    }

    @Test
    void getUserParticipationRequests_shouldReturnEmptyListForNewUser() throws Exception {
        // Предполагаем, что пользователь с ID 999 не существует в тестовых данных
        // Сначала создаем пользователя, если нужно, но в данном случае тест на 404
        mockMvc.perform(get("/users/{userId}/requests", 999)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void createParticipationRequest_shouldReturn404ForNonExistingUser() throws Exception {
        mockMvc.perform(post("/users/{userId}/requests", 999)
                        .param("eventId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(String.format(Config.NOT_FOUND_EXCEPTION, 999)));
    }

    @Test
    void createParticipationRequest_shouldReturn404ForNonExistingEvent() throws Exception {
        mockMvc.perform(post("/users/{userId}/requests", 1)
                        .param("eventId", "999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(String.format(Config.NOT_FOUND_EXCEPTION, 999)));
    }

    @Test
    void createParticipationRequest_shouldHandleEventWithZeroParticipantLimit() throws Exception {
        // Находим событие с participantLimit = 0 и устанавливаем его как опубликованное
        // Предполагаем, что событие с ID=6 имеет participantLimit = 0
        eventRepository.findById(6L).ifPresent(event -> {
            event.setState(EventState.PUBLISHED);
            eventRepository.save(event);
        });

        mockMvc.perform(post("/users/{userId}/requests", 3)
                        .param("eventId", "6")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.event").value(6))
                .andExpect(jsonPath("$.requester").value(3))
                .andExpect(jsonPath("$.status").value("CONFIRMED")); // Должен быть подтвержден, так как лимит 0 = безлимитно
    }

    @Test
    void createParticipationRequest_shouldReturn409ForCanceledEvent() throws Exception {
        // Событие в состоянии CANCELED
        // Предполагаем, что событие с ID=7 находится в состоянии CANCELED
        mockMvc.perform(post("/users/{userId}/requests", 3)
                        .param("eventId", "7")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(Config.REQUEST_UNPUBLISHED_EVENT_EXCEPTION_MESSAGE));
    }

    @Test
    void cancelParticipationRequest_shouldReturn404ForNonExistingUser() throws Exception {
        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", 999, 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(String.format(Config.NOT_FOUND_EXCEPTION, 1)));
    }

    @Test
    void cancelParticipationRequest_shouldHandleAlreadyCanceledRequest() throws Exception {
        // Сначала отменяем запрос
        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", 2, 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));

        // Пытаемся отменить уже отмененный запрос - должен вернуть 200 с тем же статусом
        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", 2, 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    void cancelParticipationRequest_shouldHandleRejectedRequest() throws Exception {
        // Тест для запроса в статусе REJECTED
        // Предполагаем, что запрос с ID=4 находится в статусе REJECTED
        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", 2, 4)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.status").value("CANCELED")); // Должен остаться CANCELED или измениться?
    }

    @Test
    void getUserParticipationRequests_shouldHandleUserWithMixedStatusRequests() throws Exception {
        // Пользователь с запросами в разных статусах (PENDING, CONFIRMED, CANCELED, REJECTED)
        // Предполагаем, что пользователь с ID=5 имеет запросы в разных статусах
        mockMvc.perform(get("/users/{userId}/requests", 5)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3)) // 3 запроса с разными статусами
                .andExpect(jsonPath("$[?(@.status=='PENDING')]").exists())
                .andExpect(jsonPath("$[?(@.status=='CONFIRMED')]").exists())
                .andExpect(jsonPath("$[?(@.status=='CANCELED')]").exists());
    }

    @Test
    void createParticipationRequest_shouldReturn409ForEventWithRequestModerationFalseButLimitReached() throws Exception {
        // Событие без модерации, но с достигнутым лимитом участников
        // Предполагаем, что событие с ID=8: requestModeration=false, participantLimit=1, confirmedRequests=1


        eventRepository.findById(8L).ifPresent(event -> {
            event.setState(EventState.PUBLISHED);
            event.setRequestModeration(false);
            event.setParticipantLimit(1);
            event.setConfirmedRequests(1);
            eventRepository.save(event);
        });

        mockMvc.perform(post("/users/{userId}/requests", 3)
                        .param("eventId", "8")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(Config.REQUEST_PARTICIPANT_LIMIT_EXCEPTION_MESSAGE));
    }

    @Test
    void createParticipationRequest_shouldCreateConfirmedForEventWithModerationButNoLimit() throws Exception {
        // Событие с модерацией, но без лимита (participantLimit = 0)
        // Предполагаем, что событие с ID=9: requestModeration=true, participantLimit=0
        eventRepository.findById(9L).ifPresent(event -> {
            event.setState(EventState.PUBLISHED);
            eventRepository.save(event);
        });

        mockMvc.perform(post("/users/{userId}/requests", 3)
                        .param("eventId", "9")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.event").value(9))
                .andExpect(jsonPath("$.requester").value(3))
                .andExpect(jsonPath("$.status").value("PENDING")); // Должен быть PENDING из-за модерации
    }


    @Test
    void createParticipationRequest_shouldReturn400ForMissingEventId() throws Exception {
        // Отсутствует обязательный параметр eventId
        mockMvc.perform(post("/users/{userId}/requests", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createParticipationRequest_shouldReturn400ForInvalidEventId() throws Exception {
        // Невалидный eventId (не число)
        mockMvc.perform(post("/users/{userId}/requests", 1)
                        .param("eventId", "invalid")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelParticipationRequest_shouldReturn400ForInvalidRequestId() throws Exception {
        // Невалидный requestId в пути
        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", 1, "invalid")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
