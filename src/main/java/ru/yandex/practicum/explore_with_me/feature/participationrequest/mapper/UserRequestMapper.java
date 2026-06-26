package ru.yandex.practicum.explore_with_me.feature.participationrequest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.explore_with_me.feature.participationrequest.dto.UserRequestDto;
import ru.yandex.practicum.explore_with_me.feature.participationrequest.model.UserRequest;

@Mapper(componentModel = "spring")
public interface UserRequestMapper {

    @Mapping(target = "event", source = "event.id")
    @Mapping(target = "requester", source = "requester.id")
    UserRequestDto toDto(UserRequest request);

}
