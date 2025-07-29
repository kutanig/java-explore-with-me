package ru.practicum.ewm.service.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.CategoryMapper;
import ru.practicum.ewm.model.category.Category;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        log.info("Adding new category with name: {}", newCategoryDto.getName());

        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            log.warn("Category with name {} already exists", newCategoryDto.getName());
            throw new ConflictException("Category with this name already exists");
        }

        Category category = Category.builder()
                .name(newCategoryDto.getName())
                .build();

        Category savedCategory = categoryRepository.save(category);
        log.debug("Category added successfully with ID: {}", savedCategory.getId());
        return categoryMapper.mapToCategoryDto(savedCategory);
    }

    @Override
    public void deleteCategory(Long catId) {
        log.info("Attempting to delete category with ID: {}", catId);
        if (eventRepository.existsByCategoryId(catId)) {
            log.warn("Cannot delete category ID {} as it's not empty", catId);
            throw new ConflictException("The category is not empty");
        }
        categoryRepository.deleteById(catId);
        log.debug("Category with ID {} deleted successfully", catId);
    }

    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        log.info("Updating category with ID: {}", catId);
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> {
                    log.error("Category not found with ID: {}", catId);
                    return new NotFoundException("Category not found");
                });
        category.setName(categoryDto.getName());
        Category updatedCategory = categoryRepository.save(category);
        log.debug("Category with ID {} updated successfully", catId);
        return categoryMapper.mapToCategoryDto(updatedCategory);
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        log.info("Fetching categories from {} with size {}", from, size);
        Pageable pageable = PageRequest.of(from / size, size);
        List<CategoryDto> categories = categoryRepository.findAll(pageable).stream()
                .map(categoryMapper::mapToCategoryDto)
                .collect(Collectors.toList());
        log.debug("Found {} categories", categories.size());
        return categories;
    }

    @Override
    public CategoryDto getCategory(Long catId) {
        log.info("Fetching category with ID: {}", catId);
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> {
                    log.error("Category not found with ID: {}", catId);
                    return new NotFoundException("Category not found");
                });
        log.debug("Category with ID {} found", catId);
        return categoryMapper.mapToCategoryDto(category);
    }
}