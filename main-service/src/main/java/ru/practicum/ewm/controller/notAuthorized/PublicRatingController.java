package ru.practicum.ewm.controller.notAuthorized;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.rating.EventRatingDto;
import ru.practicum.ewm.dto.rating.UserRatingDto;
import ru.practicum.ewm.service.rating.RatingService;

import java.util.List;

@RestController
@RequestMapping("/rating")
@RequiredArgsConstructor
@Slf4j
public class PublicRatingController {

    private final RatingService ratingService;

    @GetMapping("/events/{eventId}")
    public EventRatingDto getEventRating(@PathVariable Long eventId) {
        log.info("Public request for event {} rating", eventId);
        return ratingService.getEventRating(eventId);
    }

    @GetMapping("/users/{userId}")
    public UserRatingDto getUserRating(@PathVariable Long userId) {
        log.info("Public request for user {} rating", userId);
        return ratingService.getUserRating(userId);
    }

    @GetMapping("/users/top")
    public List<UserRatingDto> getTopUsersByRating() {
        log.info("Public request for top-rated users");
        return ratingService.getTopUsersByRating();
    }
}