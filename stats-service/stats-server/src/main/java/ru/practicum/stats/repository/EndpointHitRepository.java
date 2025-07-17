package ru.practicum.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface EndpointHitRepository extends JpaRepository<EndpointHit, Long> {
    @Query("SELECT new ru.practicum.stats.dto.ViewStats(e.app, e.uri, COUNT(e)) " +
            "FROM EndpointHit e " +
            "WHERE e.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR e.uri IN :uris) " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY COUNT(e) DESC")
    List<ViewStats> getStats(@Param("start") LocalDateTime start,
                             @Param("end") LocalDateTime end,
                             @Param("uris") List<String> uris);

    @Query("SELECT new ru.practicum.stats.dto.ViewStats(e.app, e.uri, COUNT(DISTINCT e.ip)) " +
            "FROM EndpointHit e " +
            "WHERE e.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR e.uri IN :uris) " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY COUNT(DISTINCT e.ip) DESC")
    List<ViewStats> getStatsUnique(@Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end,
                                   @Param("uris") List<String> uris);
} 