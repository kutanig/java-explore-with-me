package ru.practicum.ewm.service.validation;

import java.time.LocalDateTime;
import java.util.List;

public interface ValidationService {

    void validatePublicEventsSearchParams(String text,
                                          List<Long> categories,
                                          Boolean paid,
                                          LocalDateTime rangeStart,
                                          LocalDateTime rangeEnd,
                                          Boolean onlyAvailable,
                                          String sort,
                                          Integer from,
                                          Integer size);

    void validateAdminEventsSearchParams(List<Long> users,
                                         List<String> states,
                                         List<Long> categories,
                                         LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd,
                                         Integer from,
                                         Integer size);

    void validateDateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd);

    void validateSortParameter(String sort);

    void validatePaginationParams(Integer from, Integer size);

    void validateEventStates(List<String> states);

    /**
     * Валидация даты события - должна быть в будущем
     */
    void validateEventDate(LocalDateTime eventDate);

    /**
     * Валидация даты события для обновления - должна быть в будущем
     */
    void validateEventDateForUpdate(LocalDateTime eventDate);

    /**
     * Валидация названия категории
     */
    void validateCategoryName(String name);
} 