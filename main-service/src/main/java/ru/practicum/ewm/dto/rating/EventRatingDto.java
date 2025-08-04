package ru.practicum.ewm.dto.rating;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRatingDto {
    private Long eventId;
    private Long likes;
    private Long dislikes;
    private Double rating;

    public EventRatingDto(Long eventId, Long likes, Long dislikes) {
        this.eventId = eventId;
        this.likes = likes;
        this.dislikes = dislikes;
        this.rating = calculateRating(likes, dislikes);
    }

    private double calculateRating(Long likes, Long dislikes) {
        if (likes + dislikes > 0) {
            return ((double) (likes - dislikes) / (likes + dislikes)) * 100;
        }
        return 0.0;
    }

    public static EventRatingDto of(Long eventId, Long likes, Long dislikes) {
        return new EventRatingDto(eventId, likes, dislikes);
    }
}