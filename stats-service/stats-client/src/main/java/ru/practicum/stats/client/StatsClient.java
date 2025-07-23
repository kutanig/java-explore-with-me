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
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class StatsClient {
    private final RestTemplate restTemplate;
    private final String serverUrl;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(RestTemplate restTemplate,
                       @Value("${services.stats-service.server-url:http://localhost:9090}")
                       String serverUrl) {
        this.restTemplate = restTemplate;
        this.serverUrl = serverUrl;
        log.info("Initializing StatsClient with server URL: {}", serverUrl);
    }

    public void saveHit(EndpointHit hit) {
        log.debug("Sending hit to stats service: {}", hit);
        try {
            restTemplate.postForEntity(serverUrl + "/hit", hit, Void.class);
            log.debug("Successfully sent hit to stats service");
        } catch (Exception e) {
            log.error("Failed to send hit to stats service", e);
            throw e;
        }
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end,
                                    @Nullable List<String> uris, boolean unique) {
        log.debug("Requesting stats from {} to {}, uris: {}, unique: {}",
                start, end, uris, unique);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverUrl + "/stats")
                .queryParam("start", FORMATTER.format(start))
                .queryParam("end", FORMATTER.format(end))
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            builder.queryParam("uris", String.join(",", uris));
        }

        try {
            ResponseEntity<ViewStats[]> response = restTemplate.getForEntity(
                    builder.toUriString(),
                    ViewStats[].class
            );
            log.debug("Received stats response with {} items", response.getBody().length);
            return Arrays.asList(response.getBody());
        } catch (Exception e) {
            log.error("Failed to get stats from stats service", e);
            throw e;
        }
    }
}
