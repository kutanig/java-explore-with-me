package ru.practicum.ewm.service.compilation;

import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto saveCompilation(NewCompilationDto newCompilationDto);
    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);
    CompilationDto getCompilationById(Long compId);
    void deleteCompilation(Long compId);
    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest);
}
