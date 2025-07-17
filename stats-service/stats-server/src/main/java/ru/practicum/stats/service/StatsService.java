package ru.practicum.stats.service;

import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StatsService {
    EndpointHit saveHit(EndpointHit hitDto);
    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, Optional<List<String>> uris, boolean unique);
} 