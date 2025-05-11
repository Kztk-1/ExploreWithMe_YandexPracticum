package ru.yandex.practicum.explore_with_me.feature.category.controller;

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
import ru.yandex.practicum.explore_with_me.feature.category.dto.NewCategoryDto;
import ru.yandex.practicum.explore_with_me.feature.category.dto.UpdateCategoryDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = {"/clear-data.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PublicCategoryControllerIntegrationTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    /**
     * В test-data.sql мы заранее вставили 5 категорий с id=1..5 и именами:
     *   1→Music, 2→Sport, 3→Theatre, 4→Exhibition, 5→Workshop
     */
    @Test
    void getCategories_shouldReturnAllAndRespectPagination() throws Exception {
        // без пагинации: from=0, size=10 → вернёт все 5
        mockMvc.perform(get("/categories")
                        .param("from", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                // проверим, что в списке есть категория с id=3 и name="Theatre"
                .andExpect(jsonPath("$[?(@.id==3)].name").value("Theatre"));

        // пагинация: from=0, size=2 → первые 2 (id=1,2)
        mockMvc.perform(get("/categories")
                        .param("from", "0")
                        .param("size", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getCategoryById_shouldReturnDto_whenExists() throws Exception {
        // в тестовых данных id=2 → name="Sport"
        mockMvc.perform(get("/categories/{id}", 2)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Sport"));
    }

    @Test
    void getCategoryById_shouldReturn404_whenNotFound() throws Exception {
        long nonexistentId = 999L; // в test-data.sql только 1..5

        mockMvc.perform(get("/categories/{id}", nonexistentId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Category with id=" + nonexistentId + " not found"));
    }

    @Test
    void addCategory_shouldReturnCreatedCategory() throws Exception {
        NewCategoryDto dto = new NewCategoryDto();
        dto.setName("NewCat");

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("NewCat"));
    }

    @Test
    void updateCategory_shouldUpdateAndReturnDto() throws Exception {
        UpdateCategoryDto dto = new UpdateCategoryDto();
        dto.setName("UpdatedCat");

        mockMvc.perform(patch("/admin/categories/{catId}", 3)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("UpdatedCat"));
    }

    @Test
    void deleteCategory_shouldRemoveCategory() throws Exception {
        mockMvc.perform(delete("/admin/categories/{catId}", 5))
                .andExpect(status().isNoContent());

        // Убедимся, что категория действительно удалена
        mockMvc.perform(get("/categories/{id}", 5))
                .andExpect(status().isNotFound());
    }
}
