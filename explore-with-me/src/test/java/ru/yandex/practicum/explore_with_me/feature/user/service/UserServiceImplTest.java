package ru.yandex.practicum.explore_with_me.feature.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.yandex.practicum.explore_with_me.exception.ConflictException;
import ru.yandex.practicum.explore_with_me.feature.user.dto.NewUserRequest;
import ru.yandex.practicum.explore_with_me.feature.user.dto.UserDto;
import ru.yandex.practicum.explore_with_me.feature.user.mapper.UserMapper;
import ru.yandex.practicum.explore_with_me.feature.user.model.User;
import ru.yandex.practicum.explore_with_me.feature.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional // Откатывает транзакции после каждого теста
@RequiredArgsConstructor(onConstructor_ = @Autowired)
//@Sql(scripts = {"/clear-data.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"/clear-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserServiceImplTest {

    private final UserService userService;

    private final UserRepository userRepository;

    private final UserMapper userMapper;


    @Test
    void createUser_shouldSaveUserAndReturnDto() {
        String name = "New User";
        String email = "new@example.com";
        NewUserRequest request = new NewUserRequest(name, email);

        UserDto createdUser = userService.createUser(request);

        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getEmail()).isEqualTo("new@example.com");
        assertThat(createdUser.getRegistrationDate().isBefore(LocalDateTime.now().plusSeconds(2))).isTrue();
        assertThat(createdUser.getRegistrationDate().isAfter(LocalDateTime.now().minusSeconds(2))).isTrue();
        assertThat(userRepository.findById(createdUser.getId())).isPresent();
    }

    @Test
    void getUsers_shouldReturnAllUsers() {
        //1
        String name1 = "name1", name2 = "name2", name3 = "name3";
        String email1 = "email1", email2 = "email2", email3 = "email3";
        NewUserRequest request1 = new NewUserRequest(name1, email1);
        NewUserRequest request2 = new NewUserRequest(name2, email2);
        NewUserRequest request3 = new NewUserRequest(name3, email3);

        //2
        UserDto createdUser1 = userService.createUser(request1);
        UserDto createdUser2 = userService.createUser(request2);
        UserDto createdUser3 = userService.createUser(request3);

        List<Long> ids = List.of(1L, 2L, 3L);
        Pageable pageable = PageRequest.of(0, 10);
        List<UserDto> result = userService.getUsers(ids, pageable);

        //3
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(UserDto::getId)
                .containsExactlyInAnyOrder(createdUser1.getId(), createdUser2.getId(), createdUser3.getId());


    }

    @Test
    void getUsers_shouldReturnTwoUsers() {
        //1
        String name1 = "name1", name2 = "name2", name3 = "name3";
        String email1 = "email1", email2 = "email2", email3 = "email3";
        NewUserRequest request1 = new NewUserRequest(name1, email1);
        NewUserRequest request2 = new NewUserRequest(name2, email2);
        NewUserRequest request3 = new NewUserRequest(name3, email3);

        //2
        UserDto createdUser1 = userService.createUser(request1);
        UserDto createdUser2 = userService.createUser(request2);
        UserDto createdUser3 = userService.createUser(request3);

        List<Long> ids = List.of(1L, 2L);
        Pageable pageable = PageRequest.of(0, 10);
        List<UserDto> result = userService.getUsers(ids, pageable);

        //3
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(UserDto::getId)
                .containsExactlyInAnyOrder(createdUser1.getId(), createdUser2.getId());
    }

    @Test
    void getUser_shouldReturnUser() {
        //1
        String name1 = "name1", name2 = "name2", name3 = "name3";
        String email1 = "email1", email2 = "email2", email3 = "email3";
        NewUserRequest request1 = new NewUserRequest(name1, email1);
        NewUserRequest request2 = new NewUserRequest(name2, email2);
        NewUserRequest request3 = new NewUserRequest(name3, email3);

        //2
        UserDto createdUser1 = userService.createUser(request1);
        UserDto createdUser2 = userService.createUser(request2);
        UserDto createdUser3 = userService.createUser(request3);
        //2.1
        Optional<User> gotUser1 = userService.getUser(createdUser1.getId());
        Optional<User> gotUser2 = userService.getUser(createdUser3.getId() + 1); //Wrong id!!!
        Optional<User> gotUser3 = userService.getUser(createdUser3.getId());

        //3.1
        assertThat(gotUser1).isPresent();
        assertThat(gotUser1.get().getId()).isEqualTo(createdUser1.getId());
        //3.2
        assertThat(gotUser2).isEmpty();
        //3.3
        assertThat(gotUser3).isPresent();
        assertThat(gotUser3.get().getId()).isEqualTo(createdUser3.getId());
    }

    @Test
    void deleteUser_shouldDeleteUser() {
        //1
        String name1 = "name1", name2 = "name2", name3 = "name3";
        String email1 = "email1", email2 = "email2", email3 = "email3";
        NewUserRequest request1 = new NewUserRequest(name1, email1);
        NewUserRequest request2 = new NewUserRequest(name2, email2);
        NewUserRequest request3 = new NewUserRequest(name3, email3);

        //2
        UserDto createdUser1 = userService.createUser(request1);
        UserDto createdUser2 = userService.createUser(request2);
        UserDto createdUser3 = userService.createUser(request3);
        //DELETING user2
        userService.deleteUser(createdUser2.getId());

//        List<Long> ids = List.of(1L, 2L, 3L);
        List<Long> ids = null; // all users
        Pageable pageable = PageRequest.of(0, 10);
        List<UserDto> result = userService.getUsers(ids, pageable);



        //3
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(UserDto::getId)
                .containsExactlyInAnyOrder(createdUser1.getId(), createdUser3.getId());
    }

}
