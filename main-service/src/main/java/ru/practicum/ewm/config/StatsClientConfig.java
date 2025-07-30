package ru.practicum.ewm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.practicum.stats.client.StatsClient;

@Configuration
public class StatsClientConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public StatsClient statsClient(RestTemplate restTemplate) {
        String statsServerUrl = System.getenv("STATS_SERVER_URL");
        if (statsServerUrl == null) {
            statsServerUrl = "http://localhost:9090";
        }
        return new StatsClient(restTemplate, statsServerUrl);
    }
}