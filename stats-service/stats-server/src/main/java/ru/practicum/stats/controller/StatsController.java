package ru.practicum.stats.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.service.StatsService;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void hit(@Valid @RequestBody EndpointHitDto hitDto) {
        log.info("Received hit request: app={}, uri={}, ip={}",
                hitDto.getApp(), hitDto.getUri(), hitDto.getIp());
        statsService.saveHit(hitDto);
        log.debug("Hit saved successfully");
    }

    @GetMapping("/stats")
    public ResponseEntity<List<ViewStats>> getStats(
            @RequestParam("start") String startParam,
            @RequestParam("end") String endParam,
            @RequestParam(value = "uris", required = false) List<String> uris,
            @RequestParam(value = "unique", required = false, defaultValue = "false") Boolean unique) {

        String decodedStart = URLDecoder.decode(startParam, StandardCharsets.UTF_8);
        String decodedEnd = URLDecoder.decode(endParam, StandardCharsets.UTF_8);

        LocalDateTime start;
        LocalDateTime end;
        try {
            start = LocalDateTime.parse(decodedStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            end = LocalDateTime.parse(decodedEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException e) {
            log.warn("Invalid date format: start={}, end={}", decodedStart, decodedEnd);
            throw new IllegalArgumentException("Invalid date format. Required format: yyyy-MM-dd HH:mm:ss");
        }

        log.info("Received stats request: start={}, end={}, uris={}, unique={}",
                start, end, uris, unique);

        if (start.isAfter(end)) {
            log.warn("Invalid time range: start date {} is after end date {}", start, end);
            throw new IllegalArgumentException("Start date must be before end date");
        }

        List<String> decodedUris = uris != null ?
                uris.stream()
                        .map(uri -> URLDecoder.decode(uri, StandardCharsets.UTF_8))
                        .collect(Collectors.toList()) :
                null;

        List<ViewStats> stats = statsService.getStats(start, end, decodedUris, unique);
        log.debug("Returning {} stats records", stats.size());

        return ResponseEntity.ok(stats);
    }
}