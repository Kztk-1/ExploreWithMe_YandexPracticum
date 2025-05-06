package ru.yandex.practicum.explore_with_me.feature.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.explore_with_me.feature.event.model.Event;
import ru.yandex.practicum.explore_with_me.feature.event.model.EventState;
import ru.yandex.practicum.explore_with_me.feature.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findByInitiatorId(Long userId, Pageable pageable);

    List<Event> findByIdIn(List<Long> eventIds);

    Page<Event> findByCategoryId(Long catId, Pageable pageable);

    Page<Event> findByEventDateBetweenAndState(
            LocalDateTime start,
            LocalDateTime end,
            EventState state,
            Pageable pageable
    );

}
