package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.repository.EndpointHitRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StatsServiceImpl implements StatsService {
    private final EndpointHitRepository repository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public EndpointHit saveHit(EndpointHit hitDto) {
        log.debug("Saving hit: app={}, uri={}, ip={}, timestamp={}",
                hitDto.getApp(), hitDto.getUri(), hitDto.getIp(), hitDto.getTimestamp());

        ru.practicum.stats.model.EndpointHit entity = ru.practicum.stats.model.EndpointHit.builder()
                .app(hitDto.getApp())
                .uri(hitDto.getUri())
                .ip(hitDto.getIp())
                .timestamp(LocalDateTime.parse(hitDto.getTimestamp(), FORMATTER))
                .build();

        ru.practicum.stats.model.EndpointHit saved = repository.save(entity);
        hitDto.setId(saved.getId());

        log.info("Hit saved successfully with id={}", saved.getId());
        return hitDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end,
                                    Optional<List<String>> uris, boolean unique, String app) {
        log.debug("Getting stats: start={}, end={}, uris={}, unique={}, app={}",
                start, end, uris.orElse(null), unique, app);

        List<String> uriList = uris.orElse(null);
        if (uriList != null && uriList.isEmpty()) {
            uriList = null;
        }

        List<ViewStats> stats;

        if (unique) {
            log.debug("Getting unique stats");
            stats = repository.getStatsUnique(start, end, uriList);
        } else {
            log.debug("Getting non-unique stats");
            stats = repository.getStats(start, end, uriList);
        }

        log.info("Returning {} stats records", stats.size());
        return stats;
    }
}