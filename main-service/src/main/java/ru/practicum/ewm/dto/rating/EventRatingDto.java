package ru.practicum.ewm.dto.rating;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventRatingDto {
    private Long eventId;
    private Long likes;
    private Long dislikes;
    private Double rating;
    
    public static EventRatingDto of(Long eventId, Long likes, Long dislikes) {
        double rating = 0.0;
        if (likes + dislikes > 0) {
            rating = ((double) (likes - dislikes) / (likes + dislikes)) * 100;
        }
        
        return EventRatingDto.builder()
                .eventId(eventId)
                .likes(likes)
                .dislikes(dislikes)
                .rating(rating)
                .build();
    }
}