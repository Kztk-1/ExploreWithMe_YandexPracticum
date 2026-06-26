package ru.yandex.practicum.explore_with_me.feature.participationrequest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.explore_with_me.feature.participationrequest.model.RequestStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDto {
    private Long id;
    private Long event; // eventId
    private Long requester; // userId
    private LocalDateTime created;
    private RequestStatus status;
}