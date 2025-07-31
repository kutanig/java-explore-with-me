package ru.practicum.stats.dto;

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EndpointHit {
    private Long id;
    
    @NotBlank(message = "App cannot be blank")
    private String app;
    
    @NotBlank(message = "URI cannot be blank")
    private String uri;
    
    @NotBlank(message = "IP cannot be blank")
    private String ip;
    
    @NotBlank(message = "Timestamp cannot be blank")
    private String timestamp;
}
