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
import ru.yandex.practicum.explore_with_me.feature.event.dto.UpdateEventAdminRequest;
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
class AdminEventControllerIntegrationTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void getEvents_shouldFilterByStatesAndCategories() throws Exception {
        // Тестовые данные:
        // События с state=PUBLISHED: id=1,3,4
        // События с category_id=2: id=2 (Marathon)
        mockMvc.perform(get("/admin/events")
                        .param("states", "PUBLISHED")
                        .param("categories", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0)) // Нет пересечения фильтров

                .andExpect(jsonPath("$[0]").doesNotExist());

        // Проверка фильтра по states=PUBLISHED
        mockMvc.perform(get("/admin/events")
                        .param("states", "PUBLISHED")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[?(@.id==1)].title").value("Jazz Night"));
    }

    @Test
    void getEvents_shouldFilterByDateRange() throws Exception {
        LocalDateTime start = LocalDateTime.parse("2025-08-01 00:00:00", formatter);
        LocalDateTime end = LocalDateTime.parse("2025-09-30 23:59:59", formatter);

        // В диапазон попадают события:
        // id=3 (2025-08-12) и id=4 (2025-09-01)
        mockMvc.perform(get("/admin/events")
                        .param("rangeStart", start.format(formatter))
                        .param("rangeEnd", end.format(formatter))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[1].id").value(4));
    }

    @Test
    void getEvents_shouldPaginateResults() throws Exception {
        // Всего 5 событий, проверяем пагинацию
        mockMvc.perform(get("/admin/events")
                        .param("from", "2")
                        .param("size", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[1].id").value(4));
    }

    @Test
    void updateEventAdmin_shouldPublishEvent() throws Exception {
        UpdateEventAdminRequest updateRequest = new UpdateEventAdminRequest();
        updateRequest.setStateAction(StateAction.PUBLISH_EVENT);

        // Событие id=2 изначально в состоянии PENDING
        mockMvc.perform(patch("/admin/events/{eventId}", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("PUBLISHED"))
                .andExpect(jsonPath("$.publishedOn").isNotEmpty());
    }

    @Test
    void updateEventAdmin_shouldRejectEvent() throws Exception {
        UpdateEventAdminRequest updateRequest = new UpdateEventAdminRequest();
        updateRequest.setStateAction(StateAction.REJECT_EVENT);

        // Попытка отклонить уже опубликованное событие id=1
        mockMvc.perform(patch("/admin/events/{eventId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(Config.EVENT_PUBLISH_CONFLICT_EXCEPTION_MESSAGE));
    }

    @Test
    void updateEventAdmin_shouldReturn404ForNonExistingEvent() throws Exception {
        UpdateEventAdminRequest updateRequest = new UpdateEventAdminRequest();
        updateRequest.setTitle("New Title");

        mockMvc.perform(patch("/admin/events/{eventId}", 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(String.format(Config.NOT_FOUND_EXCEPTION, 999)));
    }
}