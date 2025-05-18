package ru.yandex.practicum.explore_with_me.feature.event.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.explore_with_me.feature.event.model.Location;

import java.time.LocalDateTime;

/**
 * DTO для PATCH /admin/events/{eventId}
 *
 * Все поля опциональны — админ может отправить в теле запроса только те, которые нужно поменять.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventAdminRequest {

    @Size(min = 3, max = 120)
    private String title;

    @Size(min = 20, max = 2000)
    private String annotation;

    private Integer category;

    @Size(min = 20, max = 7000)
    private String description;

    @Future
    private LocalDateTime eventDate;

    private Location location;

    private Boolean paid;

    @PositiveOrZero
    private Integer participantLimit;

    private Boolean requestModeration;

    /**
     * Действие над статусом события:
     * например, PUBLISH_EVENT, REJECT_EVENT
     * (только для Admin)
     */
    private String stateAction;
}
