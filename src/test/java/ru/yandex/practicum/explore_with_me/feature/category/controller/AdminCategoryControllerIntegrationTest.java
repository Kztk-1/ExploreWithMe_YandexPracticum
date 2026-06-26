package ru.yandex.practicum.explore_with_me.feature.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.explore_with_me.BaseIntegrationTest;
import ru.yandex.practicum.explore_with_me.config.Config;
import ru.yandex.practicum.explore_with_me.feature.category.dto.CategoryDto;
import ru.yandex.practicum.explore_with_me.feature.category.dto.NewCategoryDto;
import ru.yandex.practicum.explore_with_me.feature.category.dto.UpdateCategoryDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminCategoryControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void addCategory_shouldCreateNewCategory() throws Exception {
        NewCategoryDto newCategory = new NewCategoryDto("New Category");

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategory)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("New Category"));
    }

    @Test
    void addCategory_shouldConflictForDuplicateName() throws Exception {
        NewCategoryDto duplicateCategory = new NewCategoryDto("Music");

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateCategory)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateCategory_shouldModifyExistingCategory() throws Exception {
        UpdateCategoryDto updateDto = new UpdateCategoryDto("Updated Exhibition");

        mockMvc.perform(patch("/admin/categories/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.name").value("Updated Exhibition"));
    }

    @Test
    void updateCategory_shouldNotFoundForInvalidId() throws Exception {
        UpdateCategoryDto updateDto = new UpdateCategoryDto("Invalid Update");

        mockMvc.perform(patch("/admin/categories/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCategory_shouldSucceedForUnusedCategory() throws Exception {
        // Создаем новую категорию, которая не используется
        NewCategoryDto newCategory = new NewCategoryDto("Temporary Category");

        MvcResult createResult = mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategory)))
                .andExpect(status().isCreated())
                .andReturn();

        // Извлекаем ID созданной категории
        CategoryDto createdCategory = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                CategoryDto.class
        );
        Long newCategoryId = createdCategory.getId();

        // Удаляем созданную категорию
        mockMvc.perform(delete("/admin/categories/{id}", newCategoryId))
                .andExpect(status().isNoContent());

        // Проверяем что категория удалена
        mockMvc.perform(get("/categories/{id}", newCategoryId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCategory_shouldConflictForUsedCategory() throws Exception {
        // Используем ID категории, которая реально используется (из тестовых данных)
        long usedCategoryId = 1L; // Категория Music используется в событии 1

        mockMvc.perform(delete("/admin/categories/{id}", usedCategoryId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(Config.CATEGORY_ALREADY_USED_MESSAGE));
    }
}