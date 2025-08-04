package ru.practicum.ewm.dto.rating;

import lombok.*;
import ru.practicum.ewm.model.rating.RatingType;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RatingDto {
    private Long id;
    private Long userId;
    private Long eventId;
    private RatingType type;
    private LocalDateTime createdOn;
}