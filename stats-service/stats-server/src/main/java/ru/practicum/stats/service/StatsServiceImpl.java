package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.repository.EndpointHitRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class StatsServiceImpl implements StatsService {
    private final EndpointHitRepository repository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public EndpointHit saveHit(EndpointHit hitDto) {
        ru.practicum.stats.model.EndpointHit entity = ru.practicum.stats.model.EndpointHit.builder()
                .app(hitDto.getApp())
                .uri(hitDto.getUri())
                .ip(hitDto.getIp())
                .timestamp(LocalDateTime.parse(hitDto.getTimestamp(), FORMATTER))
                .build();
        ru.practicum.stats.model.EndpointHit saved = repository.save(entity);
        hitDto.setId(saved.getId());
        return hitDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, Optional<List<String>> uris, boolean unique) {
        List<String> uriList = uris.orElse(null);
        if (unique) {
            return repository.getStatsUnique(start, end, uriList);
        } else {
            return repository.getStats(start, end, uriList);
        }
    }
} 