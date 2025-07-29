package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.model.participationRequest.ParticipationRequest;
import ru.practicum.ewm.model.participationRequest.ParticipationRequestStatus;


import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {
    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    List<ParticipationRequest> findAllByRequesterId(Long requesterId);

    Optional<ParticipationRequest> findByIdAndRequesterId(Long id, Long requesterId);

    List<ParticipationRequest> findAllByEventId(Long eventId);

    long countByEventIdAndStatus(Long eventId, ParticipationRequestStatus status);

    List<ParticipationRequest> findAllByEventIdAndStatus(Long eventId, ParticipationRequestStatus status);

    @Query("SELECT r FROM ParticipationRequest r " +
            "WHERE r.event.id IN :eventIds AND r.status = :status")
    List<ParticipationRequest> findAllByEventIdInAndStatus(@Param("eventIds") List<Long> eventIds,
                                                           @Param("status") ParticipationRequestStatus status);

    @Query("SELECT r.event.id as eventId, COUNT(r) as count " +
            "FROM ParticipationRequest r " +
            "WHERE r.event.id IN :eventIds AND r.status = :status " +
            "GROUP BY r.event.id")
    List<RequestCountProjection> countByEventIdInAndStatus(
            @Param("eventIds") List<Long> eventIds,
            @Param("status") ParticipationRequestStatus status);

    interface RequestCountProjection {
        Long getEventId();
        Long getCount();
    }
}
