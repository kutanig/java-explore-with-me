package ru.practicum.ewm.model.compilation;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.ewm.model.event.Event;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "compilations")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false)
    private Boolean pinned;

    @ManyToMany
    @JoinTable(
            name = "compilation_events",
            joinColumns = @JoinColumn(name = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private Set<Event> events = new HashSet<>();
}
