package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.dto.rating.EventRatingDto;
import ru.practicum.ewm.dto.rating.UserRatingDto;
import ru.practicum.ewm.model.rating.Rating;
import ru.practicum.ewm.model.rating.RatingType;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    
    Optional<Rating> findByUserIdAndEventId(Long userId, Long eventId);
    
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.event.id = :eventId AND r.type = :type")
    Long countByEventIdAndType(@Param("eventId") Long eventId, @Param("type") RatingType type);
    
    @Query("SELECT new ru.practicum.ewm.dto.rating.EventRatingDto(" +
           "r.event.id, " +
           "SUM(CASE WHEN r.type = 'LIKE' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN r.type = 'DISLIKE' THEN 1 ELSE 0 END)) " +
           "FROM Rating r WHERE r.event.id = :eventId GROUP BY r.event.id")
    Optional<EventRatingDto> getEventRating(@Param("eventId") Long eventId);
    
    @Query("SELECT new ru.practicum.ewm.dto.rating.EventRatingDto(" +
           "r.event.id, " +
           "SUM(CASE WHEN r.type = 'LIKE' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN r.type = 'DISLIKE' THEN 1 ELSE 0 END)) " +
           "FROM Rating r WHERE r.event.id IN :eventIds GROUP BY r.event.id")
    List<EventRatingDto> getEventsRatings(@Param("eventIds") List<Long> eventIds);
    
    @Query("SELECT new ru.practicum.ewm.dto.rating.UserRatingDto(" +
           "e.initiator.id, " +
           "e.initiator.name, " +
           "SUM(CASE WHEN r.type = 'LIKE' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN r.type = 'DISLIKE' THEN 1 ELSE 0 END), " +
           "COUNT(DISTINCT e.id)) " +
           "FROM Rating r JOIN r.event e WHERE e.initiator.id = :userId GROUP BY e.initiator.id, e.initiator.name")
    Optional<UserRatingDto> getUserRating(@Param("userId") Long userId);
    
    @Query("SELECT new ru.practicum.ewm.dto.rating.UserRatingDto(" +
           "e.initiator.id, " +
           "e.initiator.name, " +
           "SUM(CASE WHEN r.type = 'LIKE' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN r.type = 'DISLIKE' THEN 1 ELSE 0 END), " +
           "COUNT(DISTINCT e.id)) " +
           "FROM Rating r JOIN r.event e " +
           "GROUP BY e.initiator.id, e.initiator.name " +
           "ORDER BY (SUM(CASE WHEN r.type = 'LIKE' THEN 1 ELSE 0 END) - SUM(CASE WHEN r.type = 'DISLIKE' THEN 1 ELSE 0 END)) DESC")
    List<UserRatingDto> getTopUsersByRating();
}