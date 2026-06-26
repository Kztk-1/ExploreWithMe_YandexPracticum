package ru.yandex.practicum.explore_with_me.feature.participationrequest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.explore_with_me.feature.participationrequest.model.UserRequest;

import java.util.List;
import java.util.Optional;

public interface UserRequestRepository extends JpaRepository<UserRequest, Long> {

    List<UserRequest> findAllByRequesterId (Long requesterId);

    Optional<UserRequest> findByIdAndRequesterId(Long id, Long requesterId);

    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    List<UserRequest> findAllByEventIdAndEventInitiatorId(Long eventId, Long initiatorId);

    List<UserRequest> findAllByEventIdAndIdIn(Long eventId, List<Long> ids);
}
