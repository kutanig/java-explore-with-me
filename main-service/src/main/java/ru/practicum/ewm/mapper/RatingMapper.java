package ru.practicum.ewm.mapper;

import ru.practicum.ewm.dto.rating.RatingDto;
import ru.practicum.ewm.dto.rating.RatingRequestDto;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.rating.Rating;
import ru.practicum.ewm.model.user.User;

import java.time.LocalDateTime;

public class RatingMapper {
    
    public static RatingDto toDto(Rating rating) {
        return RatingDto.builder()
                .id(rating.getId())
                .userId(rating.getUser().getId())
                .eventId(rating.getEvent().getId())
                .type(rating.getType())
                .createdOn(rating.getCreatedOn())
                .build();
    }
    
    public static Rating toEntity(RatingRequestDto requestDto, User user, Event event) {
        return Rating.builder()
                .user(user)
                .event(event)
                .type(requestDto.getType())
                .createdOn(LocalDateTime.now())
                .build();
    }
}