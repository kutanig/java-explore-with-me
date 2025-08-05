package ru.practicum.ewm.controller.authorized;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.rating.RatingDto;
import ru.practicum.ewm.dto.rating.RatingRequestDto;
import ru.practicum.ewm.dto.rating.UserRatingDto;
import ru.practicum.ewm.service.rating.RatingService;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
@Slf4j
public class PrivateRatingController {

    private final RatingService ratingService;

    @PostMapping("/events/{eventId}/rating")
    @ResponseStatus(HttpStatus.CREATED)
    public RatingDto addOrUpdateRating(@PathVariable Long userId,
                                       @PathVariable Long eventId,
                                       @Valid @RequestBody RatingRequestDto requestDto) {
        log.info("Request to add/update rating from user {} for event {}", userId, eventId);
        return ratingService.addOrUpdateRating(userId, eventId, requestDto);
    }

    @DeleteMapping("/events/{eventId}/rating")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRating(@PathVariable Long userId,
                             @PathVariable Long eventId) {
        log.info("Request to delete rating from user {} for event {}", userId, eventId);
        ratingService.deleteRating(userId, eventId);
    }

    @GetMapping("/rating")
    public UserRatingDto getUserRating(@PathVariable Long userId) {
        log.info("Request for user {} rating", userId);
        return ratingService.getUserRating(userId);
    }
}