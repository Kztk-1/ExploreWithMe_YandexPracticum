package ru.yandex.practicum.explore_with_me.feature.event.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.explore_with_me.feature.category.model.Category;
import ru.yandex.practicum.explore_with_me.feature.user.model.User;

import java.time.LocalDateTime;


@Data
@Entity
@Builder(toBuilder = true) // toBuilder для тестов
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 2000)
    private String annotation;

    @Column(nullable = false, length = 7000)
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Embedded
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "category_id",
            nullable = true,
            foreignKey = @ForeignKey(
                    name = "fk_event_category",
                    foreignKeyDefinition =
                            "FOREIGN KEY (category_id) " +
                                    "REFERENCES category(id) " +
                                    "ON DELETE SET NULL"
            )
    )
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventState state;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean paid;

    @Column(name = "participant_limit", columnDefinition = "integer default 0")
    private Integer participantLimit;

    @Column(name = "request_moderation", columnDefinition = "boolean default true")
    private Boolean requestModeration;

    @Column(name = "confirmed_requests", columnDefinition = "integer default 0")
    private Integer confirmedRequests;

    @Column(columnDefinition = "integer default 0")
    private Long views;

    @PrePersist
    void prePersist() {
        createdOn = LocalDateTime.now();
    }
}


