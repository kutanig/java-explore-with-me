package ru.practicum.stats.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StatsClient {
    private final RestTemplate restTemplate;
    private final String serverUrl;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(RestTemplate restTemplate,
                       @Value("${stats.server.url:http://localhost:9090}")
                       String serverUrl) {
        this.restTemplate = restTemplate;
        this.serverUrl = serverUrl;
        log.info("Initializing StatsClient with server URL: {}", serverUrl);
    }

    public void recordHit(String appName, String uri, String ip, LocalDateTime timestamp) {
        EndpointHit hit = EndpointHit.builder()
                .app(appName)
                .uri(uri)
                .ip(ip)
                .timestamp(timestamp.format(FORMATTER))
                .build();
        saveHit(hit);
    }

    public void saveHit(EndpointHit hit) {
        log.debug("Sending hit to stats service: {}", hit);
        try {
            restTemplate.postForEntity(serverUrl + "/hit", hit, Void.class);
        } catch (Exception e) {
            log.error("Failed to send hit to stats service", e);
            throw new RuntimeException("Failed to save hit to stats service", e);
        }
    }

    public Long getViews(String appName, Long eventId, LocalDateTime start, LocalDateTime end, boolean unique) {
        List<String> uris = List.of("/events/" + eventId);
        List<ViewStats> stats = getStats(appName, start, end, uris, unique);
        return stats.isEmpty() ? 0L : stats.get(0).getHits();
    }

    public Map<Long, Long> getViewsForEvents(String appName, List<Long> eventIds,
                                             LocalDateTime start, LocalDateTime end,
                                             boolean unique) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .collect(Collectors.toList());

        List<ViewStats> stats = getStats(appName, start, end, uris, unique);

        return stats.stream()
                .collect(Collectors.toMap(
                        s -> parseEventIdFromUri(s.getUri()),
                        ViewStats::getHits,
                        (existing, replacement) -> existing));
    }

    public List<ViewStats> getStats(String appName, LocalDateTime start, LocalDateTime end,
                                    @Nullable List<String> uris, boolean unique) {
        log.debug("Requesting stats for app {} from {} to {}, uris: {}, unique: {}",
                appName, start, end, uris, unique);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverUrl + "/stats")
                .queryParam("start", start.format(FORMATTER))
                .queryParam("end", end.format(FORMATTER))
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            for (String uri : uris) {
                builder.queryParam("uris", uri);
            }
        }

        try {
            ResponseEntity<ViewStats[]> response = restTemplate.getForEntity(
                    builder.toUriString(),
                    ViewStats[].class);
            return Arrays.asList(response.getBody());
        } catch (Exception e) {
            log.error("Failed to get stats from stats service", e);
            throw new RuntimeException("Failed to get stats from stats service", e);
        }
    }

    private Long parseEventIdFromUri(String uri) {
        try {
            return Long.parseLong(uri.substring(uri.lastIndexOf('/') + 1));
        } catch (Exception e) {
            log.error("Failed to parse event ID from URI: {}", uri, e);
            throw new IllegalArgumentException("Invalid event URI format: " + uri);
        }
    }
}
