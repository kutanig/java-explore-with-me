package ru.practicum.ewm.service.rating;

import ru.practicum.ewm.dto.rating.*;

import java.util.List;

public interface RatingService {
    
    RatingDto addOrUpdateRating(Long userId, Long eventId, RatingRequestDto requestDto);
    
    void deleteRating(Long userId, Long eventId);
    
    EventRatingDto getEventRating(Long eventId);
    
    List<EventRatingDto> getEventsRatings(List<Long> eventIds);
    
    UserRatingDto getUserRating(Long userId);
    
    List<UserRatingDto> getTopUsersByRating();
}