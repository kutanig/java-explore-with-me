package ru.practicum.ewm.dto.rating;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.practicum.ewm.model.rating.RatingType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RatingRequestDto {
    
    @NotNull
    private RatingType type;
}