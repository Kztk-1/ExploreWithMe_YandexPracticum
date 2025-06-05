package ru.yandex.practicum.explore_with_me.feature.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.explore_with_me.feature.event.model.Event;
import ru.yandex.practicum.explore_with_me.feature.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с событиями.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    /**
     * Поиск событий пользователя с пагинацией
     */
    Page<Event> findByInitiatorId(Long initiatorId, Pageable pageable);

    /**
     * Поиск конкретного события пользователя
     */
    Optional<Event> findByIdAndInitiatorId(Long id, Long initiatorId);

    Page<Event> findAllByAdminFilters(
            @Param("users")       List<Long> users,
            @Param("states")      List<EventState> states,
            @Param("categories")  List<Long> categoryIds,
            @Param("rangeStart")  LocalDateTime rangeStart,
            @Param("rangeEnd")    LocalDateTime rangeEnd,
            Pageable pageable
    );

    Page<Event> findPublic(
            @Param("text") String text,
            @Param("categories") List<Long> categories,
            @Param("paid") Boolean paid,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("onlyAvailable") Boolean onlyAvailable,
            Pageable pageable
    );
}
