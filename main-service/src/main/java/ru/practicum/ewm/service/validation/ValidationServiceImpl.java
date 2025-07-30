package ru.practicum.ewm.service.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.exception.BadRequestException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {

    public void validatePublicEventsSearchParams(String text,
                                                 List<Long> categories,
                                                 Boolean paid,
                                                 LocalDateTime rangeStart,
                                                 LocalDateTime rangeEnd,
                                                 Boolean onlyAvailable,
                                                 String sort,
                                                 Integer from,
                                                 Integer size) {

        validateDateRange(rangeStart, rangeEnd);

        validateSortParameter(sort);

        validatePaginationParams(from, size);
    }

    public void validateDateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("Range start date must be before range end date");
        }
    }

    public void validateSortParameter(String sort) {
        if (sort != null && !sort.equals("EVENT_DATE") && !sort.equals("VIEWS")) {
            throw new BadRequestException("Invalid sort parameter. Must be EVENT_DATE or VIEWS");
        }
    }

    public void validatePaginationParams(Integer from, Integer size) {
        if (from < 0) {
            throw new BadRequestException("From parameter must be non-negative");
        }
        if (size <= 0) {
            throw new BadRequestException("Size parameter must be positive");
        }
    }

    public void validateAdminEventsSearchParams(List<Long> users,
                                                List<String> states,
                                                List<Long> categories,
                                                LocalDateTime rangeStart,
                                                LocalDateTime rangeEnd,
                                                Integer from,
                                                Integer size) {

        validateDateRange(rangeStart, rangeEnd);

        validatePaginationParams(from, size);

        validateEventStates(states);
    }

    public void validateEventStates(List<String> states) {
        if (states != null) {
            for (String state : states) {
                try {
                    ru.practicum.ewm.model.event.EventState.valueOf(state);
                } catch (IllegalArgumentException e) {
                    throw new BadRequestException("Invalid event state: " + state);
                }
            }
        }
    }

    @Override
    public void validateEventDate(LocalDateTime eventDate) {
        if (eventDate != null && eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Event date must be at least 2 hours in the future");
        }
    }

    @Override
    public void validateEventDateForUpdate(LocalDateTime eventDate) {
        if (eventDate != null && eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Event date must be at least 2 hours in the future");
        }
    }

    @Override
    public void validateCategoryName(String name) {
        if (name != null && (name.length() < 1 || name.length() > 50)) {
            throw new BadRequestException("Category name must be between 1 and 50 characters");
        }
    }
}