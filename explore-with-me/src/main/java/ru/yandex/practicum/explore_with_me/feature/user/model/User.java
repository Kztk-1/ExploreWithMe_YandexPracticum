package ru.yandex.practicum.explore_with_me.feature.user.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.explore_with_me.config.Config;

import java.time.LocalDateTime;

@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = Config.USER_NAME_MAX_LENGTH)
    private String name;

    @Column(unique = true, nullable = false, length = Config.USER_EMAIL_MAX_LENGTH)
    private String email;
}