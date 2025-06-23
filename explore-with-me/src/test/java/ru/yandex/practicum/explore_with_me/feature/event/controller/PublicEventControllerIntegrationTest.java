package ru.yandex.practicum.explore_with_me.feature.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.explore_with_me.config.Config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = {"/clear-data.sql", "/test-data.sql"})
public class PublicEventControllerIntegrationTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Тест 1: Получение всех опубликованных событий
    @Test
    void getPublicEvents_shouldReturnPublishedEvents() throws Exception {
        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3)) // Опубликовано 3 события (id=1,3,4)
                .andExpect(jsonPath("$[?(@.id==1)].title").value("Jazz Night"))
                .andExpect(jsonPath("$[?(@.id==3)].title").value("Shakespeare in the Park"));
    }

    // Тест 2: Фильтрация по тексту
    @Test
    void getPublicEvents_shouldFilterByText() throws Exception {
        mockMvc.perform(get("/events")
                        .param("text", "marathon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0)) // Событие 2 не опубликовано
                .andExpect(jsonPath("$[0]").doesNotExist());
    }

    // Тест 3: Фильтрация по категориям
    @Test
    void getPublicEvents_shouldFilterByCategories() throws Exception {
        mockMvc.perform(get("/events")
                        .param("categories", "1,3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.id==1)]").exists())
                .andExpect(jsonPath("$[?(@.id==3)]").exists());
    }

    // Тест 4: Фильтрация по датам
    @Test
    void getPublicEvents_shouldFilterByDateRange() throws Exception {
        LocalDateTime start = LocalDateTime.parse("2025-08-01 00:00:00", formatter);
        LocalDateTime end = LocalDateTime.parse("2025-09-30 23:59:59", formatter);

        mockMvc.perform(get("/events")
                        .param("rangeStart", start.format(formatter))
                        .param("rangeEnd", end.format(formatter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.id==3)]").exists())
                .andExpect(jsonPath("$[?(@.id==4)]").exists());
    }

    // Тест 5: Только доступные события
    @Test
    void getPublicEvents_shouldShowOnlyAvailable() throws Exception {
        mockMvc.perform(get("/events")
                        .param("onlyAvailable", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)) // События 1 и 3 имеют свободные места
                .andExpect(jsonPath("$[?(@.id==4)]").doesNotExist());
    }

    // Тест 6: Сортировка по просмотрам
    @Test
    void getPublicEvents_shouldSortByViews() throws Exception {
        mockMvc.perform(get("/events")
                        .param("sort", "VIEWS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3)) // 300 просмотров
                .andExpect(jsonPath("$[1].id").value(1)) // 150
                .andExpect(jsonPath("$[2].id").value(4)); // 75
    }

    // Тест 7: Получение несуществующего события
    @Test
    void getEventById_shouldReturn404() throws Exception {
        mockMvc.perform(get("/events/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(String.format(Config.NOT_FOUND_EXCEPTION, 999)));
    }

    // Тест 8: Некорректные параметры пагинации
    @Test
    void getPublicEvents_shouldHandleBadPagination() throws Exception {
        mockMvc.perform(get("/events")
                        .param("from", "-1")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }
}