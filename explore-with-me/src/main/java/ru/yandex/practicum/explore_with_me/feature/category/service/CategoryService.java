package ru.yandex.practicum.explore_with_me.feature.category.service;

import org.springframework.data.domain.PageRequest;
import ru.yandex.practicum.explore_with_me.feature.category.dto.CategoryDto;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> getCategories(PageRequest pageRequest);
    CategoryDto getCategory(Long catId);
}
