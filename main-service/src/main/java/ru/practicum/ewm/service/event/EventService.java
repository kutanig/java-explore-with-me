package ru.practicum.ewm.service.event;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.NewEventDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventFullDto getEvent(Long id, HttpServletRequest request);
    EventFullDto addEvent(Long userId, NewEventDto newEventDto);
    List<EventShortDto> searchEvents(String text, List<Long> categories, Boolean paid,
                                     LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                     Boolean onlyAvailable, String sort,
                                     Integer from, Integer size, HttpServletRequest request);
}
