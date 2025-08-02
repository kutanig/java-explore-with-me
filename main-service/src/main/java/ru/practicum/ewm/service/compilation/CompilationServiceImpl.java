package ru.practicum.ewm.service.compilation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.CompilationMapper;
import ru.practicum.ewm.model.compilation.Compilation;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.participationRequest.ParticipationRequestStatus;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.stats.client.StatsClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;

    @Override
    public CompilationDto saveCompilation(NewCompilationDto newCompilationDto) {
        log.info("Saving new compilation with title: {}", newCompilationDto.getTitle());

        Compilation compilation = Compilation.builder()
                .title(newCompilationDto.getTitle())
                .pinned(newCompilationDto.getPinned())
                .build();

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(newCompilationDto.getEvents()));
            compilation.setEvents(events);
        }

        Compilation savedCompilation = compilationRepository.save(compilation);
        log.info("Compilation saved with id: {}", savedCompilation.getId());

        Map<Long, Long> confirmedRequests = getConfirmedRequestsCounts(savedCompilation.getEvents());
        Map<Long, Long> views = getViewsCounts(savedCompilation.getEvents());

        return compilationMapper.toDto(savedCompilation, confirmedRequests, views);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        log.info("Getting compilations with pinned={}, from={}, size={}", pinned, from, size);
        Pageable pageable = PageRequest.of(from / size, size);

        List<Compilation> compilations = pinned != null
                ? compilationRepository.findAllByPinned(pinned, pageable)
                : compilationRepository.findAll(pageable).getContent();

        Set<Event> allEvents = compilations.stream()
                .flatMap(c -> c.getEvents().stream())
                .collect(Collectors.toSet());

        Map<Long, Long> confirmedRequests = getConfirmedRequestsCounts(allEvents);
        Map<Long, Long> views = getViewsCounts(allEvents);

        return compilations.stream()
                .map(c -> compilationMapper.toDto(c, confirmedRequests, views))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long compId) {
        log.info("Getting compilation by id: {}", compId);
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> {
                    log.error("Compilation not found with id: {}", compId);
                    return new NotFoundException("Compilation not found");
                });

        Map<Long, Long> confirmedRequests = getConfirmedRequestsCounts(compilation.getEvents());
        Map<Long, Long> views = getViewsCounts(compilation.getEvents());

        return compilationMapper.toDto(compilation, confirmedRequests, views);
    }

    @Override
    public void deleteCompilation(Long compId) {
        log.info("Deleting compilation with id: {}", compId);
        if (!compilationRepository.existsById(compId)) {
            log.error("Compilation not found for deletion with id: {}", compId);
            throw new NotFoundException("Compilation not found");
        }
        compilationRepository.deleteById(compId);
        log.debug("Compilation with id {} deleted successfully", compId);
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest) {
        log.info("Updating compilation with id: {}", compId);
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> {
                    log.error("Compilation not found for update with id: {}", compId);
                    return new NotFoundException("Compilation not found");
                });

        if (updateRequest.getTitle() != null) {
            compilation.setTitle(updateRequest.getTitle());
        }
        if (updateRequest.getPinned() != null) {
            compilation.setPinned(updateRequest.getPinned());
        }
        if (updateRequest.getEvents() != null) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(updateRequest.getEvents()));
            compilation.setEvents(events);
        }

        Compilation updatedCompilation = compilationRepository.save(compilation);
        log.info("Compilation with id {} updated successfully", compId);

        Map<Long, Long> confirmedRequests = getConfirmedRequestsCounts(updatedCompilation.getEvents());
        Map<Long, Long> views = getViewsCounts(updatedCompilation.getEvents());

        return compilationMapper.toDto(updatedCompilation, confirmedRequests, views);
    }

    private Map<Long, Long> getConfirmedRequestsCounts(Collection<Event> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        return requestRepository.countByEventIdInAndStatus(eventIds, ParticipationRequestStatus.CONFIRMED)
                .stream()
                .collect(Collectors.toMap(
                        RequestRepository.RequestCountProjection::getEventId,
                        RequestRepository.RequestCountProjection::getCount
                ));
    }

    private Map<Long, Long> getViewsCounts(Collection<Event> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        return statsClient.getViewsForEvents("ewm-main-service",
                eventIds,
                LocalDateTime.now().minusYears(1),
                LocalDateTime.now(),
                true);
    }
}
