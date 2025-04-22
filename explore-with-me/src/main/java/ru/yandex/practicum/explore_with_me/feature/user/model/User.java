package ru.yandex.practicum.explore_with_me.feature.user.model;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(nullable = false, length = 50)
    private String name;

    @Column(unique = true, nullable = false, length = 100)
    private String email;
}