package ru.practicum.ewm.dto.rating;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRatingDto {
    private Long userId;
    private String userName;
    private Long totalLikes;
    private Long totalDislikes;
    private Double rating;
    private Long eventsCount;

    public UserRatingDto(Long userId, String userName, Long totalLikes, Long totalDislikes, Long eventsCount) {
        this.userId = userId;
        this.userName = userName;
        this.totalLikes = totalLikes;
        this.totalDislikes = totalDislikes;
        this.eventsCount = eventsCount;
        this.rating = calculateRating(totalLikes, totalDislikes);
    }

    private double calculateRating(Long likes, Long dislikes) {
        if (likes + dislikes > 0) {
            return ((double) (likes - dislikes) / (likes + dislikes)) * 100;
        }
        return 0.0;
    }

    public static UserRatingDto of(Long userId, String userName, Long totalLikes, Long totalDislikes, Long eventsCount) {
        return new UserRatingDto(userId, userName, totalLikes, totalDislikes, eventsCount);
    }
}