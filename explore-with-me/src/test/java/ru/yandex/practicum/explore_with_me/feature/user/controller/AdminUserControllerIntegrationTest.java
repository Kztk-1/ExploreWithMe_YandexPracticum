package ru.yandex.practicum.explore_with_me.feature.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.explore_with_me.feature.user.dto.NewUserRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = {"/clear-data.sql", "/test-data.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AdminUserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {
        NewUserRequest request = NewUserRequest.builder()
                .name("New Test User")
                .email("newuser@example.com")
                .build();

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("New Test User"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"));
    }

    @Test
    void createUser_shouldReturn409ForDuplicateEmail() throws Exception {
        NewUserRequest request = NewUserRequest.builder()
                .name("Duplicate Email")
                .email("alice.johnson@example.com") // Используем email, который уже есть в test-data.sql
                .build();


        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getUsers_withoutIds_shouldReturnAllUsers() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].name").value("Alice Johnson"))
                .andExpect(jsonPath("$[4].name").value("Eva Green"));
    }

    @Test
    void getUsers_withIds_shouldReturnFilteredUsers() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .param("ids", "1", "3", "5")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Alice Johnson"))
                .andExpect(jsonPath("$[1].id").value(3))
                .andExpect(jsonPath("$[1].name").value("Carol White"))
                .andExpect(jsonPath("$[2].id").value(5))
                .andExpect(jsonPath("$[2].name").value("Eva Green"));
    }

    @Test
    void getUsers_withPagination_shouldReturnPage() throws Exception {
        // Первая страница: 2 пользователя
        mockMvc.perform(get("/admin/users")
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        // Вторая страница: следующие 2 пользователя
        mockMvc.perform(get("/admin/users")
                        .param("from", "2")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[1].id").value(4));

        // Третья страница: последний пользователь
        mockMvc.perform(get("/admin/users")
                        .param("from", "4")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(5));
    }

    @Test
    void deleteUser_shouldRemoveUser() throws Exception {
        // Удаляем пользователя с id=2
        log.info("DEBUG 1");
        mockMvc.perform(delete("/admin/users/2"))
                .andExpect(status().isNoContent());

        log.info("DEBUG 2");
        // Проверяем, что пользователь удалён
        mockMvc.perform(get("/admin/users")
                        .param("ids", "2")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        log.info("DEBUG 3");
        // Проверяем, что остальные пользователи на месте
        mockMvc.perform(get("/admin/users")
                        .param("ids", "1", "3", "4", "5")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }
}