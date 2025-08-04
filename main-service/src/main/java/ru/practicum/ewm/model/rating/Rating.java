package ru.practicum.ewm.model.rating;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "ratings")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RatingType type;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;
}