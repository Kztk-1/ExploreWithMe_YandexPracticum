package ru.yandex.practicum.explore_with_me.feature.participationrequest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateResult {
    private List<UserRequestDto> confirmedRequests;
    private List<UserRequestDto> rejectedRequests;
}
