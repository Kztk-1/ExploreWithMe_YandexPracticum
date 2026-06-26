package ru.yandex.practicum.explore_with_me.feature.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.explore_with_me.BaseIntegrationTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
//@Sql(scripts = {"/sql/clear-data.sql", "/sql/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PublicCategoryControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getCategories_shouldReturnPaginatedResults() throws Exception {
        // First page: 2 categories
        mockMvc.perform(get("/categories")
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Exhibition"))
                .andExpect(jsonPath("$[1].name").value("Music"));

        // Second page: next 2 categories
        mockMvc.perform(get("/categories")
                        .param("from", "2")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Sport"))
                .andExpect(jsonPath("$[1].name").value("Theatre"));

        // Last page: remaining category
        mockMvc.perform(get("/categories")
                        .param("from", "4")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Workshop"));
    }

    @Test
    void getCategories_shouldReturnFullListWithoutPagingParams() throws Exception {
        MvcResult result = mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andReturn();

        List<?> categories = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                List.class
        );
        assertEquals(5, categories.size());
    }

    @Test
    void getCategory_shouldReturnCategoryDetails() throws Exception {
        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Music"));
    }

    @Test
    void getCategory_shouldNotFoundForInvalidId() throws Exception {
        mockMvc.perform(get("/categories/999"))
                .andExpect(status().isNotFound());
    }
}