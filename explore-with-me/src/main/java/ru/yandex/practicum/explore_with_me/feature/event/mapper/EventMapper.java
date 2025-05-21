package ru.yandex.practicum.explore_with_me.feature.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.explore_with_me.feature.event.dto.EventFullDto;
import ru.yandex.practicum.explore_with_me.feature.event.dto.NewEventDto;
import ru.yandex.practicum.explore_with_me.feature.event.model.Event;
import ru.yandex.practicum.explore_with_me.feature.user.mapper.UserMapper;

@Mapper(componentModel = "spring",
//        uses = {UserMapper.class, CategoryMapper.class, LocationMapper.class})
        uses = {UserMapper.class})
public interface EventMapper {
public abstract class EventMapper {

    EventFullDto toDto(Event event);

    // ВАЖНО!!! Category и Initiator будем делать в сервисе!!!
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "paid", source = "paid")
    Event toEntity(NewEventDto dto);
}