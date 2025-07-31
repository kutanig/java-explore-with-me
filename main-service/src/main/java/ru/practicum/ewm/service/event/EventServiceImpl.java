package ru.practicum.ewm.service.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.mapper.LocationMapper;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.model.category.Category;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.event.EventState;
import ru.practicum.ewm.model.participationRequest.ParticipationRequest;
import ru.practicum.ewm.model.participationRequest.ParticipationRequestStatus;
import ru.practicum.ewm.model.user.User;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.service.validation.ValidationService;
import ru.practicum.stats.client.StatsClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private final RequestMapper requestMapper;
    private final StatsClient statsClient;
    private final ValidationService validationService;

    private static final String APP_NAME = "ewm-main-service";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getPublicEvent(Long id, HttpServletRequest request) {
        log.info("Getting public event with id: {}", id);

        Event event = eventRepository.findByIdAndState(id, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found"));

        long confirmedRequests = requestRepository.countByEventIdAndStatus(id, ParticipationRequestStatus.CONFIRMED);

        statsClient.recordHit(APP_NAME, request.getRequestURI(), request.getRemoteAddr(), LocalDateTime.now());

        long views = statsClient.getViews(APP_NAME, id, LocalDateTime.of(2020, 1, 1, 0, 0, 0), LocalDateTime.now(), true);

        log.info("Found public event: {}", event.getTitle());
        return eventMapper.toFullDto(event, confirmedRequests, views);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> searchPublicEvents(String text,
                                                  List<Long> categories,
                                                  Boolean paid,
                                                  LocalDateTime rangeStart,
                                                  LocalDateTime rangeEnd,
                                                  Boolean onlyAvailable,
                                                  String sort,
                                                  Integer from,
                                                  Integer size,
                                                  HttpServletRequest request) {
        log.info("Searching public events with text: {}, categories: {}, paid: {}, rangeStart: {}, rangeEnd: {}, onlyAvailable: {}, sort: {}, from: {}, size: {}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }

        Sort sortObj = Sort.by(Sort.Direction.DESC, "eventDate");
        if ("EVENT_DATE".equals(sort)) {
            sortObj = Sort.by(Sort.Direction.ASC, "eventDate");
        } else if ("VIEWS".equals(sort)) {
            sortObj = Sort.by(Sort.Direction.DESC, "id");
        }

        Pageable pageable = PageRequest.of(from / size, size, sortObj);

        List<Event> events = eventRepository.findPublishedEvents(text, categories, paid, rangeStart, rangeEnd, pageable);

        if (onlyAvailable != null && onlyAvailable) {
            events = events.stream()
                    .filter(event -> {
                        long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), ParticipationRequestStatus.CONFIRMED);
                        return event.getParticipantLimit() == 0 || confirmedRequests < event.getParticipantLimit();
                    })
                    .collect(Collectors.toList());
        }

        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());

        statsClient.recordHit(APP_NAME, request.getRequestURI(), request.getRemoteAddr(), LocalDateTime.now());

        Map<Long, Long> viewsMap = statsClient.getViewsForEvents(APP_NAME, eventIds,
                LocalDateTime.of(2020, 1, 1, 0, 0, 0), LocalDateTime.now(), true);

        Map<Long, Long> confirmedRequestsMap = eventIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> requestRepository.countByEventIdAndStatus(id, ParticipationRequestStatus.CONFIRMED)
                ));

        log.info("Found {} public events", events.size());
        return events.stream()
                .map(event -> eventMapper.toShortDto(event,
                        confirmedRequestsMap.get(event.getId()),
                        viewsMap.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        log.info("Getting events for user: {}, from: {}, size: {}", userId, from, size);

        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findByInitiatorId(userId, pageable);

        Map<Long, Long> confirmedRequestsMap = events.stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        event -> requestRepository.countByEventIdAndStatus(event.getId(), ParticipationRequestStatus.CONFIRMED)
                ));

        log.info("Found {} events for user {}", events.size(), userId);
        return events.stream()
                .map(event -> eventMapper.toShortDto(event,
                        confirmedRequestsMap.get(event.getId()),
                        0L))
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        log.info("Adding new event for user: {}, title: {}", userId, newEventDto.getTitle());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id=" + newEventDto.getCategory() + " was not found"));

        LocalDateTime eventDate = LocalDateTime.parse(newEventDto.getEventDate(), FORMATTER);
        validationService.validateEventDate(eventDate);

        Event event = Event.builder()
                .annotation(newEventDto.getAnnotation())
                .category(category)
                .description(newEventDto.getDescription())
                .eventDate(eventDate)
                .initiator(user)
                .location(newEventDto.getLocation() != null ? locationMapper.toModel(newEventDto.getLocation()) : null)
                .paid(newEventDto.getPaid() != null ? newEventDto.getPaid() : false)
                .participantLimit(newEventDto.getParticipantLimit() != null ? newEventDto.getParticipantLimit() : 0)
                .requestModeration(newEventDto.getRequestModeration() != null ? newEventDto.getRequestModeration() : true)
                .title(newEventDto.getTitle())
                .state(EventState.PENDING)
                .createdOn(LocalDateTime.now())
                .build();

        Event savedEvent = eventRepository.save(event);
        log.info("Created event with id: {}", savedEvent.getId());

        long views = statsClient.getViews(APP_NAME, savedEvent.getId(), LocalDateTime.of(2020, 1, 1, 0, 0, 0), LocalDateTime.now(), true);

        return eventMapper.toFullDto(savedEvent, 0L, views);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        log.info("Getting event: {} for user: {}", eventId, userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);

        long views = statsClient.getViews(APP_NAME, eventId, LocalDateTime.of(2020, 1, 1, 0, 0, 0), LocalDateTime.now(), true);

        log.info("Found event: {}", event.getTitle());
        return eventMapper.toFullDto(event, confirmedRequests, views);
    }

    @Override
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        log.info("Updating event: {} for user: {}", eventId, userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id=" + updateRequest.getCategory() + " was not found"));
            event.setCategory(category);
        }
        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEventDate() != null) {
            LocalDateTime eventDate = LocalDateTime.parse(updateRequest.getEventDate(), FORMATTER);
            validationService.validateEventDateForUpdate(eventDate);
            event.setEventDate(eventDate);
        }
        if (updateRequest.getLocation() != null) {
            event.setLocation(locationMapper.toModel(updateRequest.getLocation()));
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }

        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction()) {
                case "SEND_TO_REVIEW":
                    event.setState(EventState.PENDING);
                    break;
                case "CANCEL_REVIEW":
                    event.setState(EventState.CANCELED);
                    break;
                default:
                    throw new BadRequestException("Invalid state action: " + updateRequest.getStateAction());
            }
        }

        Event savedEvent = eventRepository.save(event);
        long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);

        long views = statsClient.getViews(APP_NAME, eventId, LocalDateTime.of(2020, 1, 1, 0, 0, 0), LocalDateTime.now(), true);

        log.info("Updated event: {}", savedEvent.getTitle());
        return eventMapper.toFullDto(savedEvent, confirmedRequests, views);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {
        log.info("Getting participants for event: {} by user: {}", eventId, userId);

        eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        List<ParticipationRequest> requests = requestRepository.findAllByEventId(eventId);

        log.info("Found {} participation requests for event {}", requests.size(), eventId);

        return requests.stream()
                .map(requestMapper::mapToParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId,
                                                              Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        log.info("Changing request status for event: {} by user: {}", eventId, userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getState() == EventState.CANCELED) {
            throw new ConflictException("Cannot change request status for canceled event");
        }

        List<ParticipationRequest> requests = requestRepository.findAllById(updateRequest.getRequestIds());

        for (ParticipationRequest request : requests) {
            if (!request.getEvent().getId().equals(eventId)) {
                throw new ConflictException("Request " + request.getId() + " does not belong to event " + eventId);
            }
            if (request.getStatus() != ParticipationRequestStatus.PENDING) {
                throw new ConflictException("Request " + request.getId() + " is not in PENDING status");
            }
        }

        ParticipationRequestStatus newStatus = ParticipationRequestStatus.valueOf(updateRequest.getStatus());

        if (newStatus == ParticipationRequestStatus.CONFIRMED) {
            long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
            if (event.getParticipantLimit() > 0 && confirmedRequests + requests.size() > event.getParticipantLimit()) {
                throw new ConflictException("Cannot confirm requests: participant limit exceeded");
            }
        }

        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        for (ParticipationRequest request : requests) {
            request.setStatus(newStatus);
            ParticipationRequest savedRequest = requestRepository.save(request);

            if (newStatus == ParticipationRequestStatus.CONFIRMED) {
                confirmedRequests.add(requestMapper.mapToParticipationRequestDto(savedRequest));
            } else if (newStatus == ParticipationRequestStatus.REJECTED) {
                rejectedRequests.add(requestMapper.mapToParticipationRequestDto(savedRequest));
            }
        }

        log.info("Updated {} requests for event {}: {} confirmed, {} rejected",
                requests.size(), eventId, confirmedRequests.size(), rejectedRequests.size());

        return new EventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getAdminEvents(List<Long> users,
                                             List<String> states,
                                             List<Long> categories,
                                             LocalDateTime rangeStart,
                                             LocalDateTime rangeEnd,
                                             Integer from,
                                             Integer size) {
        log.info("Getting admin events with users: {}, states: {}, categories: {}, rangeStart: {}, rangeEnd: {}, from: {}, size: {}",
                users, states, categories, rangeStart, rangeEnd, from, size);

        List<EventState> eventStates = null;
        if (states != null) {
            eventStates = states.stream()
                    .map(EventState::valueOf)
                    .collect(Collectors.toList());
        }

        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAdminEvents(users, eventStates, categories, rangeStart, rangeEnd, pageable);

        Map<Long, Long> confirmedRequestsMap = events.stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        event -> requestRepository.countByEventIdAndStatus(event.getId(), ParticipationRequestStatus.CONFIRMED)
                ));

        log.info("Found {} admin events", events.size());
        return events.stream()
                .map(event -> eventMapper.toFullDto(event,
                        confirmedRequestsMap.get(event.getId()),
                        0L))
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest updateRequest) {
        log.info("Updating admin event: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id=" + updateRequest.getCategory() + " was not found"));
            event.setCategory(category);
        }
        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEventDate() != null) {
            LocalDateTime eventDate = LocalDateTime.parse(updateRequest.getEventDate(), FORMATTER);
            validationService.validateEventDateForUpdate(eventDate);
            event.setEventDate(eventDate);
        }
        if (updateRequest.getLocation() != null) {
            event.setLocation(locationMapper.toModel(updateRequest.getLocation()));
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }

        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction()) {
                case "PUBLISH_EVENT":
                    if (event.getState() != EventState.PENDING) {
                        throw new ConflictException("Cannot publish event that is not in PENDING state");
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case "REJECT_EVENT":
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new ConflictException("Cannot reject published event");
                    }
                    event.setState(EventState.CANCELED);
                    break;
            }
        }

        Event savedEvent = eventRepository.save(event);
        long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);

        log.info("Updated admin event: {}", savedEvent.getTitle());
        return eventMapper.toFullDto(savedEvent, confirmedRequests, 0L);
    }
}
