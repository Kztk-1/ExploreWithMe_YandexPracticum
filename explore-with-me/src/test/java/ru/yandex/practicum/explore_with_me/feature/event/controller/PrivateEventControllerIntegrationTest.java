package ru.yandex.practicum.explore_with_me.feature.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.explore_with_me.config.Config;
import ru.yandex.practicum.explore_with_me.feature.event.dto.EventFullDto;
import ru.yandex.practicum.explore_with_me.feature.event.dto.NewEventDto;
import ru.yandex.practicum.explore_with_me.feature.event.dto.UpdateEventUserRequest;
import ru.yandex.practicum.explore_with_me.feature.event.model.Location;
import ru.yandex.practicum.explore_with_me.feature.event.model.StateAction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = {"/clear-data.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PrivateEventControllerIntegrationTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void createEvent_shouldCreateNewEvent() throws Exception {
        NewEventDto newEvent = NewEventDto.builder()
                .title("New Concert")
                .annotation("New annotation")
                .description("Detailed description")
                .eventDate(LocalDateTime.now().plusDays(10))
                .categoryId(1)
                .location(Location.builder().lat(52.3740F).lon(4.8897F).build())
                .paid(true)
                .participantLimit(50)
                .requestModeration(false)
                .build();

        mockMvc.perform(post("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEvent)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Concert"))
                .andExpect(jsonPath("$.state").value("PENDING"));
    }

    @Test
    void getUserEvents_shouldReturnUserEvents() throws Exception {
        // У пользователя 1 есть 1 событие (id=1)
        mockMvc.perform(get("/users/1/events")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Jazz Night"));
    }

    @Test
    void getUserEventById_shouldReturnEvent() throws Exception {
        mockMvc.perform(get("/users/1/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.annotation").value("An evening of smooth jazz."));
    }

    @Test
    void getUserEventById_shouldReturn404IfNotExists() throws Exception {
        mockMvc.perform(get("/users/1/events/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(String.format(Config.NOT_FOUND_EXCEPTION, 999)));
    }

    @Test
    void updateEventByUser_shouldUpdateEvent() throws Exception {
        UpdateEventUserRequest updateRequest = UpdateEventUserRequest.builder()
                .annotation("Updated annotation")
                .description("Updated description")
                .build();

        mockMvc.perform(patch("/users/2/events/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.annotation").value("Updated annotation"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void updateEventByUser_shouldReturn409IfPublished() throws Exception {
        UpdateEventUserRequest updateRequest = UpdateEventUserRequest.builder()
                .title("Try to update")
                .build();

        // Событие id=1 уже опубликовано
        mockMvc.perform(patch("/users/1/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(Config.CONFLICT_EXCEPTION_MESSAGE));
    }
}