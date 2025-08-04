package ru.practicum.ewm.dto.rating;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRatingDto {
    private Long userId;
    private String userName;
    private Long totalLikes;
    private Long totalDislikes;
    private Double rating;
    private Long eventsCount;
    
    public static UserRatingDto of(Long userId, String userName, Long totalLikes, Long totalDislikes, Long eventsCount) {
        double rating = 0.0;
        if (totalLikes + totalDislikes > 0) {
            rating = ((double) (totalLikes - totalDislikes) / (totalLikes + totalDislikes)) * 100;
        }
        
        return UserRatingDto.builder()
                .userId(userId)
                .userName(userName)
                .totalLikes(totalLikes)
                .totalDislikes(totalDislikes)
                .rating(rating)
                .eventsCount(eventsCount)
                .build();
    }
}