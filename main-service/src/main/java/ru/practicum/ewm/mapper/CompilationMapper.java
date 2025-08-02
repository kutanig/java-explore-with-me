package ru.practicum.ewm.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.model.compilation.Compilation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CompilationMapper {
    private final EventMapper eventMapper;

    public CompilationDto toDto(Compilation compilation,
                                Map<Long, Long> confirmedRequests,
                                Map<Long, Long> views) {
        if (compilation == null) {
            return null;
        }

        List<EventShortDto> eventDtos = (compilation.getEvents() != null ? compilation.getEvents().stream()
                .map(event -> eventMapper.toShortDto(
                        event,
                        confirmedRequests.getOrDefault(event.getId(), 0L),
                        views.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toList()) : List.of());

        return CompilationDto.builder()
                .id(compilation.getId())
                .events(eventDtos)
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }
}
