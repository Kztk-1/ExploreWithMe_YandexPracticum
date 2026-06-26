package ru.yandex.practicum.explore_with_me.feature.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.explore_with_me.exception.ConflictException;
import ru.yandex.practicum.explore_with_me.exception.NotFoundException;
import ru.yandex.practicum.explore_with_me.feature.user.dto.NewUserRequest;
import ru.yandex.practicum.explore_with_me.feature.user.dto.UserDto;
import ru.yandex.practicum.explore_with_me.feature.user.mapper.UserMapper;
import ru.yandex.practicum.explore_with_me.feature.user.model.User;
import ru.yandex.practicum.explore_with_me.feature.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_shouldSaveAndReturnUserDto() {
        // given
        NewUserRequest request = new NewUserRequest("test@example.com", "Test User");
        User user = User.builder().name("Test User").email("test@example.com").build();
        User savedUser = User.builder().id(1L).name("Test User").email("test@example.com").build();
        UserDto expectedDto = new UserDto(1L, "test@example.com", "Test User");

        when(userMapper.toEntity(request)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toDto(savedUser)).thenReturn(expectedDto);

        // when
        UserDto result = userService.createUser(request);

        // then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test User", result.getName());

        verify(userMapper).toEntity(request);
        verify(userRepository).save(user);
        verify(userMapper).toDto(savedUser);
    }

    @Test
    void createUser_shouldThrowConflictExceptionWhenEmailExists() {
        // given
        NewUserRequest request = new NewUserRequest("test@example.com", "Test User");
        User user = User.builder().name("Test User").email("test@example.com").build();

        when(userMapper.toEntity(request)).thenReturn(user);
        when(userRepository.save(user)).thenThrow(DataIntegrityViolationException.class);

        // when & then
        assertThrows(ConflictException.class, () -> userService.createUser(request));

        verify(userMapper).toEntity(request);
        verify(userRepository).save(user);
        verifyNoMoreInteractions(userMapper);
    }

    @Test
    void getUsers_shouldReturnUsersWithIds() {
        // given
        List<Long> ids = List.of(1L, 2L);
        Pageable pageable = PageRequest.of(0, 10);
        User user1 = User.builder().id(1L).name("User1").email("user1@example.com").build();
        User user2 = User.builder().id(2L).name("User2").email("user2@example.com").build();
        Page<User> userPage = new PageImpl<>(List.of(user1, user2));

        UserDto dto1 = new UserDto(1L, "user1@example.com", "User1");
        UserDto dto2 = new UserDto(2L, "user2@example.com", "User2");

        when(userRepository.findAllByIdIn(ids, pageable)).thenReturn(userPage);
        when(userMapper.toDto(user1)).thenReturn(dto1);
        when(userMapper.toDto(user2)).thenReturn(dto2);

        // when
        List<UserDto> result = userService.getUsers(ids, pageable);

        // then
        assertEquals(2, result.size());
        assertEquals("user1@example.com", result.get(0).getEmail());
        assertEquals("User2", result.get(1).getName());

        verify(userRepository).findAllByIdIn(ids, pageable);
        verify(userMapper, times(2)).toDto(any(User.class));
    }

    @Test
    void getUsers_shouldReturnAllUsersWhenIdsNull() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        User user = User.builder().id(1L).name("User").email("user@example.com").build();
        Page<User> userPage = new PageImpl<>(List.of(user));
        UserDto dto = new UserDto(1L, "user@example.com", "User");

        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toDto(user)).thenReturn(dto);

        // when
        List<UserDto> result = userService.getUsers(null, pageable);

        // then
        assertEquals(1, result.size());
        assertEquals("user@example.com", result.get(0).getEmail());

        verify(userRepository).findAll(pageable);
        verify(userMapper).toDto(user);
    }

    @Test
    void getUsers_shouldReturnEmptyList() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(Collections.emptyList());

        when(userRepository.findAll(pageable)).thenReturn(emptyPage);

        // when
        List<UserDto> result = userService.getUsers(null, pageable);

        // then
        assertTrue(result.isEmpty());
        verify(userRepository).findAll(pageable);
        verifyNoInteractions(userMapper);
    }

    @Test
    void getUser_shouldReturnUserWhenFound() {
        // given
        Long userId = 1L;
        User user = User.builder().id(userId).name("Test").email("test@example.com").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        Optional<User> result = userService.getUser(userId);

        // then
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUser_shouldReturnEmptyWhenNotFound() {
        // given
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when
        Optional<User> result = userService.getUser(userId);

        // then
        assertFalse(result.isPresent());
        verify(userRepository).findById(userId);
    }

    @Test
    void deleteUser_shouldDeleteWhenUserExists() {
        // given
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);

        // when
        userService.deleteUser(userId);

        // then
        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_shouldThrowNotFoundExceptionWhenUserNotExist() {
        // given
        Long userId = 99L;
        when(userRepository.existsById(userId)).thenReturn(false);

        // when & then
        assertThrows(NotFoundException.class, () -> userService.deleteUser(userId));
        verify(userRepository).existsById(userId);
        verify(userRepository, never()).deleteById(anyLong());
    }
}