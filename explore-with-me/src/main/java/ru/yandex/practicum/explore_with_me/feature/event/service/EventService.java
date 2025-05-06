package ru.yandex.practicum.explore_with_me.feature.event.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.explore_with_me.feature.event.dto.EventFullDto;
import ru.yandex.practicum.explore_with_me.feature.event.dto.NewEventDto;

import java.util.List;
/*
Следующие шаги
Реализовать остальные методы сервиса:

Обновление события пользователем и админом.

Поиск событий с фильтрами.

Логика публикации/отмены событий.

Добавить валидацию:

Проверка прав пользователя на изменение события.

Обработка конфликтов (например, попытка изменить опубликованное событие).

Интеграция с ParticipationRequest:

Подтверждение/отклонение заявок.

Обновление confirmedRequests.

Написать тесты:

Unit-тесты для сервисов.

Интеграционные тесты через MockMvc.
 */
public interface EventService {

    EventFullDto createEvent(Long userId, NewEventDto eventDto);

    List<EventFullDto> getEventsByUserId(Long userId);
}
