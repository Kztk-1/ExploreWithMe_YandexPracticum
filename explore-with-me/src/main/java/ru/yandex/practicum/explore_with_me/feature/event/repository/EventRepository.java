package ru.yandex.practicum.explore_with_me.feature.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.explore_with_me.feature.event.model.Event;

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

}
