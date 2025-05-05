package ru.yandex.practicum.explore_with_me.feature.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yandex.practicum.explore_with_me.feature.user.dto.NewUserRequest;
import ru.yandex.practicum.explore_with_me.feature.user.dto.UserDto;
import ru.yandex.practicum.explore_with_me.feature.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void beforeEach() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
    }


    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {
        //1
        String userName = "test_user";
        String userEmail = "test@example.com";

        NewUserRequest request = NewUserRequest.builder()
                .name(userName)
                .email(userEmail)
                .build();

        UserDto expectedUser = UserDto.builder()
                .id(1L)
                .name(userName)
                .email(userEmail)
                .build();

        when(userService.createUser(any())).thenReturn(expectedUser);

        //2, 3
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(expectedUser.getId()))
                .andExpect(jsonPath("$.name").value(expectedUser.getName()))
                .andExpect(jsonPath("$.email").value(expectedUser.getEmail()));

        verify(userService).createUser(any());
    }

    @Test
    void getUsers_shouldReturnUserList() throws Exception {
        List<UserDto> expectedUsers = List.of(
                UserDto.builder().id(1L).name("user1").email("u1@example.com").build(),
                UserDto.builder().id(2L).name("user2").email("u2@example.com").build()
        );

        when(userService.getUsers(eq(List.of(1L, 2L)), any())).thenReturn(expectedUsers);

        mockMvc.perform(get("/admin/users")
                        .param("ids", "1", "2")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("user1"))
                .andExpect(jsonPath("$[0].email").value("u1@example.com"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("user2"))
                .andExpect(jsonPath("$[1].email").value("u2@example.com"));


        verify(userService).getUsers(eq(List.of(1L, 2L)), eq(PageRequest.of(0, 10)));
    }


    @Test
    void deleteUser_shouldReturnNoContent() throws Exception {
        Long userId = 1L;

        mockMvc.perform(delete("/admin/users/{userId}", userId))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(userId);
    }

}
