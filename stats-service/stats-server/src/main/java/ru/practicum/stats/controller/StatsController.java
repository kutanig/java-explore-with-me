package ru.practicum.stats.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHit hit(@RequestBody EndpointHit hitDto) {
        log.info("Received hit request: app={}, uri={}, ip={}",
                hitDto.getApp(), hitDto.getUri(), hitDto.getIp());
        EndpointHit savedHit = statsService.saveHit(hitDto);
        log.debug("Saved hit with id: {}", savedHit.getId());
        return savedHit;
    }

    @GetMapping("/stats")
    public ResponseEntity<List<ViewStats>> getStats(
            @RequestParam("start") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(value = "uris", required = false) List<String> uris,
            @RequestParam(value = "unique", required = false, defaultValue = "false") Boolean unique,
            @RequestParam(value = "app", required = false) String app
    ) {
        log.info("Received stats request: start={}, end={}, uris={}, unique={}, app={}",
                start, end, uris, unique, app);

        if (start.isAfter(end)) {
            log.warn("Invalid time range: start date {} is after end date {}", start, end);
            throw new IllegalArgumentException("Start date must be before end date");
        }

        Optional<List<String>> urisOptional = Optional.ofNullable(uris);
        if (urisOptional.isPresent() && urisOptional.get().isEmpty()) {
            urisOptional = Optional.empty();
        }

        List<ViewStats> stats = statsService.getStats(start, end, urisOptional, unique, app);
        log.debug("Returning {} stats records", stats.size());

        return ResponseEntity.ok(stats);
    }
}