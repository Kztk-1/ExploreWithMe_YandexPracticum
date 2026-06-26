package ru.yandex.practicum.explore_with_me.feature.category.service;

import org.springframework.data.domain.PageRequest;
import ru.yandex.practicum.explore_with_me.feature.category.dto.CategoryDto;
import ru.yandex.practicum.explore_with_me.feature.category.dto.NewCategoryDto;
import ru.yandex.practicum.explore_with_me.feature.category.dto.UpdateCategoryDto;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> getCategories(PageRequest pageRequest);
    CategoryDto getCategory(Long catId);

    CategoryDto addCategory(NewCategoryDto dto);
    void deleteCategory(Long catId);
    CategoryDto updateCategory(Long catId, UpdateCategoryDto dto);
}
