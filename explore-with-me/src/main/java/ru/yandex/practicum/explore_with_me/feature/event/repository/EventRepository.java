package ru.yandex.practicum.explore_with_me.feature.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
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

    @Query("""
        SELECT e
          FROM Event e
         WHERE (:users IS NULL OR e.initiator.id IN :users)
           AND (:states IS NULL OR e.state IN :states)
           AND (:categories IS NULL OR e.category.id IN :categories)
           AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart)
           AND (:rangeEnd   IS NULL OR e.eventDate <= :rangeEnd)
        """)
    Page<Event> findAllByAdminFilters(
            @Param("users")       List<Long> users,
            @Param("states")      List<EventState> states,
            @Param("categories")  List<Long> categoryIds,
            @Param("rangeStart")  LocalDateTime rangeStart,
            @Param("rangeEnd")    LocalDateTime rangeEnd,
            Pageable pageable
    );

    @Query("""
    SELECT e FROM Event e
    WHERE e.state = 'PUBLISHED'
    AND (:text IS NULL OR LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%')))
    AND (:paid IS NULL OR e.paid = :paid)
    AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart)
    AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd)
    AND (:categories IS NULL OR e.category.id IN :categories)
    """)
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
