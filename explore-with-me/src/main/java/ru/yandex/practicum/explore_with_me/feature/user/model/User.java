package ru.yandex.practicum.explore_with_me.feature.user.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.explore_with_me.config.Config;
import ru.yandex.practicum.explore_with_me.feature.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;

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

    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate;

    @OneToMany(
            mappedBy = "initiator",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Event> events;

    @PrePersist
    protected void onCreate() {
        this.registrationDate = LocalDateTime.now();
    }
}