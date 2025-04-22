package ru.yandex.practicum.explore_with_me.feature.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.explore_with_me.exception.ConflictException;
import ru.yandex.practicum.explore_with_me.exception.NotFoundException;
import ru.yandex.practicum.explore_with_me.feature.user.dto.NewUserRequest;
import ru.yandex.practicum.explore_with_me.feature.user.dto.UserDto;
import ru.yandex.practicum.explore_with_me.feature.user.mapper.UserMapper;
import ru.yandex.practicum.explore_with_me.feature.user.model.User;
import ru.yandex.practicum.explore_with_me.feature.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Primary
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest newUserRequest) {
        try {
            User user = userMapper.toEntity(newUserRequest);
            User savedUser = userRepository.save(user);
            return userMapper.toDto(savedUser);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Email already exists");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsers(List<Long> ids, Pageable pageable) {
        Page<User> usersPage;

        if (ids != null && !ids.isEmpty()) {
            usersPage = userRepository.findAllByIdIn(ids, pageable);
        } else {
            usersPage = userRepository.findAll(pageable);
        }

        return usersPage.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
        userRepository.deleteById(userId);
    }
}