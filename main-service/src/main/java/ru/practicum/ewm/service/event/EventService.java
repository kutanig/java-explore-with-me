package ru.practicum.ewm.service.event;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.participationRequest.ParticipationRequestDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventFullDto getPublicEvent(Long id, HttpServletRequest request);

    List<EventShortDto> searchPublicEvents(String text,
                                           List<Long> categories,
                                           Boolean paid,
                                           LocalDateTime rangeStart,
                                           LocalDateTime rangeEnd,
                                           Boolean onlyAvailable,
                                           String sort,
                                           Integer from,
                                           Integer size,
                                           HttpServletRequest request);

    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size);

    EventFullDto addEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getUserEvent(Long userId, Long eventId);

    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateRequest);

    List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId);

    EventRequestStatusUpdateResult changeRequestStatus(Long userId,
                                                       Long eventId,
                                                       EventRequestStatusUpdateRequest updateRequest);

    List<EventFullDto> getAdminEvents(List<Long> users,
                                      List<String> states,
                                      List<Long> categories,
                                      LocalDateTime rangeStart,
                                      LocalDateTime rangeEnd,
                                      Integer from,
                                      Integer size);

    EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest updateRequest);
}
