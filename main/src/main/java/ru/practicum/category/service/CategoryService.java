package ru.practicum.category.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto create(NewCategoryDto newCategoryDto);

    CategoryDto update(Long categoryId, CategoryDto categoryDto);

    void delete(Long categoryId);

    List<CategoryDto> getAll(Pageable pageable);

    CategoryDto getById(Long categoryId);
}
