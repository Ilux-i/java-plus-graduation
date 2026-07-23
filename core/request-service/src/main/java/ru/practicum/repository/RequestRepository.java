package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.event.dto.event.ConfirmedRequestCount;
import ru.practicum.request.ParticipationRequest;
import ru.practicum.request.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

    @Query("""
            select r
            from ParticipationRequest r
            where r.requester = :userId
            """)
    List<ParticipationRequest> findAllByUserId(long userId);

    @Query("""
            UPDATE ParticipationRequest r
            SET r.status = :state
            WHERE r.id = :requestId
            """)
    int changeState(long requestId, RequestStatus state);

    Long countByEventAndStatus(Long eventId, RequestStatus status);

    @Query("select new ru.practicum.event.dto.event.ConfirmedRequestCount(r.event, count(r.id)) " +
            "from ParticipationRequest r " +
            "where r.event in :eventIds and r.status = :status " +
            "group by r.event")
    List<ConfirmedRequestCount> countConfirmedRequestsByEventIds(List<Long> eventIds, RequestStatus status);

    List<ParticipationRequest> findAllByEvent(Long eventId);

    Optional<ParticipationRequest> findByRequesterAndEvent(Long userId, Long eventId);
}