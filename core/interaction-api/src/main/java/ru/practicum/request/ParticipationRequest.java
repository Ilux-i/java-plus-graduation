package ru.practicum.request;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.user.User;
import ru.practicum.event.model.Event;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@Table(name = "request")
@AllArgsConstructor
@NoArgsConstructor
public class ParticipationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @Column(name = "event_id")
    private Long event;

    @Column(name = "requester_id")
    private Long requester;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private RequestStatus status;
}
