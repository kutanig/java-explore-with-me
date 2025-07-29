package ru.practicum.ewm.service.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.event.EventState;
import ru.practicum.ewm.model.participationRequest.ParticipationRequest;
import ru.practicum.ewm.model.participationRequest.ParticipationRequestStatus;
import ru.practicum.ewm.model.user.User;
import ru.practicum.ewm.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestMapper requestMapper;

    @Override
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        log.info("Adding participation request for user {} to event {}", userId, eventId);

        User requester = getUserOrThrow(userId);
        Event event = getEventOrThrow(eventId);

        validateRequestCreation(userId, eventId, event);

        ParticipationRequest request = buildParticipationRequest(requester, event);
        ParticipationRequest savedRequest = requestRepository.save(request);

        log.info("Participation request created with id: {}", savedRequest.getId());
        return requestMapper.mapToParticipationRequestDto(savedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        log.info("Getting all requests for user id: {}", userId);
        getUserOrThrow(userId);

        List<ParticipationRequest> requests = requestRepository.findAllByRequesterId(userId);
        log.debug("Found {} requests for user {}", requests.size(), userId);

        return requests.stream()
                .map(requestMapper::mapToParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Canceling request {} by user {}", requestId, userId);
        ParticipationRequest request = getRequestOrThrow(requestId, userId);

        request.setStatus(ParticipationRequestStatus.CANCELED);
        ParticipationRequest updatedRequest = requestRepository.save(request);

        log.debug("Request {} canceled successfully", requestId);
        return requestMapper.mapToParticipationRequestDto(updatedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        log.info("Getting requests for event {} by user {}", eventId, userId);
        Event event = getEventByInitiatorOrThrow(eventId, userId);

        List<ParticipationRequest> requests = requestRepository.findAllByEventId(event.getId());
        log.debug("Found {} requests for event {}", requests.size(), eventId);

        return requests.stream()
                .map(requestMapper::mapToParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto confirmRequest(Long userId, Long eventId, Long reqId) {
        log.info("Confirming request {} for event {} by user {}", reqId, eventId, userId);
        return processRequest(userId, eventId, reqId, ParticipationRequestStatus.CONFIRMED);
    }

    @Override
    public ParticipationRequestDto rejectRequest(Long userId, Long eventId, Long reqId) {
        log.info("Rejecting request {} for event {} by user {}", reqId, eventId, userId);
        return processRequest(userId, eventId, reqId, ParticipationRequestStatus.REJECTED);
    }

    private ParticipationRequestDto processRequest(Long userId, Long eventId, Long reqId,
                                                   ParticipationRequestStatus status) {
        Event event = getEventByInitiatorOrThrow(eventId, userId);
        ParticipationRequest request = getRequestOrThrow(reqId);

        validateRequestBelongsToEvent(request, eventId);

        if (status == ParticipationRequestStatus.CONFIRMED) {
            validateParticipantLimit(event);
        }

        request.setStatus(status);
        ParticipationRequest updatedRequest = requestRepository.save(request);

        log.debug("Request {} updated to status {}", reqId, status);
        return requestMapper.mapToParticipationRequestDto(updatedRequest);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", userId);
                    return new NotFoundException("User not found");
                });
    }

    private Event getEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.error("Event not found with id: {}", eventId);
                    return new NotFoundException("Event not found");
                });
    }

    private Event getEventByInitiatorOrThrow(Long eventId, Long userId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> {
                    log.error("Event not found with id: {} for user: {}", eventId, userId);
                    return new NotFoundException("Event not found");
                });
    }

    private ParticipationRequest getRequestOrThrow(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.error("Request not found with id: {}", requestId);
                    return new NotFoundException("Request not found");
                });
    }

    private ParticipationRequest getRequestOrThrow(Long requestId, Long userId) {
        return requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> {
                    log.error("Request not found with id: {} for user: {}", requestId, userId);
                    return new NotFoundException("Request not found");
                });
    }

    private void validateRequestBelongsToEvent(ParticipationRequest request, Long eventId) {
        if (!request.getEvent().getId().equals(eventId)) {
            log.error("Request {} doesn't belong to event {}", request.getId(), eventId);
            throw new ConflictException("Request doesn't belong to this event");
        }
    }

    private void validateParticipantLimit(Event event) {
        if (event.getParticipantLimit() > 0 &&
                requestRepository.countByEventIdAndStatus(
                        event.getId(),
                        ParticipationRequestStatus.CONFIRMED) >= event.getParticipantLimit()) {
            log.warn("Participant limit reached for event {}", event.getId());
            throw new ConflictException("Participant limit reached");
        }
    }

    private void validateRequestCreation(Long userId, Long eventId, Event event) {
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            log.warn("Request already exists for user {} to event {}", userId, eventId);
            throw new ConflictException("Request already exists");
        }
        if (event.getInitiator().getId().equals(userId)) {
            log.warn("Initiator {} tried to request participation in own event", userId);
            throw new ConflictException("Initiator can't request participation");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            log.warn("Event {} is not published", eventId);
            throw new ConflictException("Event is not published");
        }
        if (event.getParticipantLimit() > 0 &&
                requestRepository.countByEventIdAndStatus(
                        eventId,
                        ParticipationRequestStatus.CONFIRMED) >= event.getParticipantLimit()) {
            log.warn("Participant limit reached for event {}", eventId);
            throw new ConflictException("Participant limit reached");
        }
    }

    private ParticipationRequest buildParticipationRequest(User requester, Event event) {
        return ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .status(determineRequestStatus(event))
                .build();
    }

    private ParticipationRequestStatus determineRequestStatus(Event event) {
        return !event.getRequestModeration() || event.getParticipantLimit() == 0 ?
                ParticipationRequestStatus.CONFIRMED :
                ParticipationRequestStatus.PENDING;
    }
}
