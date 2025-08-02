package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.repository.EndpointHitRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StatsServiceImpl implements StatsService {
    private final EndpointHitRepository repository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void saveHit(EndpointHitDto hitDto) {
        log.debug("Saving hit: app={}, uri={}, ip={}, timestamp={}",
                hitDto.getApp(), hitDto.getUri(), hitDto.getIp(), hitDto.getTimestamp());

        ru.practicum.stats.model.EndpointHit entity = ru.practicum.stats.model.EndpointHit.builder()
                .app(hitDto.getApp())
                .uri(hitDto.getUri())
                .ip(hitDto.getIp())
                .timestamp(LocalDateTime.parse(hitDto.getTimestamp(), FORMATTER))
                .build();

        repository.save(entity);
        log.info("Hit saved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end,
                                    List<String> uris, Boolean unique) {
        log.debug("Getting stats: start={}, end={}, uris={}, unique={}",
                start, end, uris, unique);

        List<String> uriList = uris;
        if (uriList != null && uriList.isEmpty()) {
            uriList = null;
        }

        List<ViewStats> stats;

        if (unique != null && unique) {
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