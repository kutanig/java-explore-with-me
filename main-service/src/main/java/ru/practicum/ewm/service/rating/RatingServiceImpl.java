package ru.practicum.ewm.service.rating;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.rating.*;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.RatingMapper;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.event.EventState;
import ru.practicum.ewm.model.rating.Rating;
import ru.practicum.ewm.model.user.User;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RatingRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public RatingDto addOrUpdateRating(Long userId, Long eventId, RatingRequestDto requestDto) {
        log.info("Adding/updating rating by user {} for event {}", userId, eventId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " not found"));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Cannot rate an unpublished event");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Cannot rate your own event");
        }

        Optional<Rating> existingRating = ratingRepository.findByUserIdAndEventId(userId, eventId);

        Rating rating;
        if (existingRating.isPresent()) {
            rating = existingRating.get();
            rating.setType(requestDto.getType());
            log.info("Updated rating with id={}", rating.getId());
        } else {
            rating = RatingMapper.toEntity(requestDto, user, event);
            log.info("Created new rating for user {} and event {}", userId, eventId);
        }

        rating = ratingRepository.save(rating);
        return RatingMapper.toDto(rating);
    }

    @Override
    @Transactional
    public void deleteRating(Long userId, Long eventId) {
        log.info("Deleting rating by user {} for event {}", userId, eventId);

        Rating rating = ratingRepository.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new NotFoundException("Rating not found"));

        ratingRepository.delete(rating);
        log.info("Rating with id={} deleted", rating.getId());
    }

    @Override
    public EventRatingDto getEventRating(Long eventId) {
        log.info("Getting rating for event {}", eventId);

        eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " not found"));

        return ratingRepository.getEventRating(eventId)
                .orElse(EventRatingDto.of(eventId, 0L, 0L));
    }

    @Override
    public List<EventRatingDto> getEventsRatings(List<Long> eventIds) {
        log.info("Getting ratings for {} events", eventIds.size());
        return ratingRepository.getEventsRatings(eventIds);
    }

    @Override
    public UserRatingDto getUserRating(Long userId) {
        log.info("Getting rating for user {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found"));

        return ratingRepository.getUserRating(userId)
                .orElse(UserRatingDto.of(userId, user.getName(), 0L, 0L, 0L));
    }

    @Override
    public List<UserRatingDto> getTopUsersByRating() {
        log.info("Getting top users by rating");
        return ratingRepository.getTopUsersByRating();
    }
}