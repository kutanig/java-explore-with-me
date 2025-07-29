package ru.practicum.ewm.service.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.*;
import ru.practicum.ewm.model.category.Category;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.event.EventState;
import ru.practicum.ewm.model.participationRequest.ParticipationRequestStatus;
import ru.practicum.ewm.model.user.User;
import ru.practicum.ewm.repository.*;
import ru.practicum.stats.client.StatsClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;
    private final EventMapper eventMapper;
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;
    private final LocationMapper locationMapper;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEvent(Long id, HttpServletRequest request) {
        log.info("Getting published event with id: {}", id);
        Event event = eventRepository.findByIdAndState(id, EventState.PUBLISHED)
                .orElseThrow(() -> {
                    log.error("Published event not found with id: {}", id);
                    return new NotFoundException("Event not found");
                });

        statsClient.recordHit("ewm-main-service",
                "/events/" + event.getId(),
                request.getRemoteAddr(),
                LocalDateTime.now());

        long confirmedRequests = requestRepository.countByEventIdAndStatus(
                event.getId(), ParticipationRequestStatus.CONFIRMED);
        long views = statsClient.getViews("ewm-main-service",
                event.getId(),
                LocalDateTime.now().minusYears(1),
                LocalDateTime.now(),
                true);

        return eventMapper.toFullDto(event, confirmedRequests, views);
    }

    @Override
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        log.info("Adding new event for user id: {}", userId);
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        Event event = Event.builder()
                .annotation(newEventDto.getAnnotation())
                .category(category)
                .description(newEventDto.getDescription())
                .eventDate(LocalDateTime.parse(newEventDto.getEventDate(), DATE_FORMATTER))
                .initiator(initiator)
                .paid(newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.getRequestModeration())
                .title(newEventDto.getTitle())
                .state(EventState.PENDING)
                .createdOn(LocalDateTime.now())
                .build();

        Event savedEvent = eventRepository.save(event);
        return eventMapper.toFullDto(savedEvent, 0, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> searchEvents(String text, List<Long> categories, Boolean paid,
                                            LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                            Boolean onlyAvailable, String sort,
                                            Integer from, Integer size, HttpServletRequest request) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findPublishedEvents(
                text, categories, paid, rangeStart, rangeEnd, pageable);

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        Map<Long, Long> confirmedRequests = getConfirmedRequestsCounts(eventIds);
        Map<Long, Long> views = getViewsCounts(eventIds);

        return events.stream()
                .map(event -> eventMapper.toShortDto(
                        event,
                        confirmedRequests.getOrDefault(event.getId(), 0L),
                        views.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toList());
    }

    private Map<Long, Long> getConfirmedRequestsCounts(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Map.of();
        }
        return requestRepository.countByEventIdInAndStatus(
                        eventIds,
                        ParticipationRequestStatus.CONFIRMED)
                .stream()
                .collect(Collectors.toMap(
                        RequestRepository.RequestCountProjection::getEventId,
                        RequestRepository.RequestCountProjection::getCount));
    }

    private Map<Long, Long> getViewsCounts(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Map.of();
        }
        return statsClient.getViewsForEvents(
                "ewm-main-service",
                eventIds,
                LocalDateTime.now().minusYears(1),
                LocalDateTime.now(),
                true);
    }
}
