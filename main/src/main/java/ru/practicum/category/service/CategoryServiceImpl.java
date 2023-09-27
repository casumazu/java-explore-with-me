package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ExistsException;
import ru.practicum.exception.NotAvailableException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto create(NewCategoryDto newCategoryDto) {

        if (newCategoryDto.getName() == null || newCategoryDto.getName().isEmpty()) {
            throw new ValidationException("Название категории не может быть пустым.");
        }

        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new ExistsException("Категория с именем " + newCategoryDto.getName() + " уже существует.");
        }

        return categoryMapper.toCategoryDto(
                categoryRepository.save(
                        categoryMapper.toCategory(newCategoryDto)));
    }

    @Override
    @Transactional
    public CategoryDto update(Long categoryId, CategoryDto categoryDto) {

        if (categoryDto.getName() == null || categoryDto.getName().isEmpty()) {
            throw new ValidationException("Название категории не может быть пустым.");
        }

        Category categoryToUpdate = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория с id " + categoryId + " не найдена."));

        if (!categoryToUpdate.getName().equals(categoryDto.getName()) && categoryRepository.existsByName(categoryDto.getName())) {
            throw new ExistsException("Категория с именем " + categoryDto.getName() + " уже существует.");
        }
        categoryToUpdate.setName(categoryDto.getName());
        return categoryMapper.toCategoryDto(categoryRepository.save(categoryToUpdate));
    }

    @Override
    @Transactional
    public void delete(Long categoryId) {
        if (eventRepository.existsByCategoryId(categoryId)) {
            throw new NotAvailableException("Ошибка. Категория содержит события.");
        }
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория с id " + categoryId + " не найдена."));

        categoryRepository.deleteById(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAll(Pageable pageable) {
        return categoryMapper.toCategoryDtoList(categoryRepository.findAll(pageable).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория с id " + categoryId + " не найдена."));

        return categoryMapper.toCategoryDto(category);
    }
}
