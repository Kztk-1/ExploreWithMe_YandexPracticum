package ru.yandex.practicum.explore_with_me.feature.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.explore_with_me.exception.NotFoundException;
import ru.yandex.practicum.explore_with_me.feature.category.dto.CategoryDto;
import ru.yandex.practicum.explore_with_me.feature.category.mapper.CategoryMapper;
import ru.yandex.practicum.explore_with_me.feature.category.model.Category;
import ru.yandex.practicum.explore_with_me.feature.category.repository.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories(PageRequest pageRequest) {
        return categoryRepository
                .findAll(pageRequest).stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategory(Long catId) {
        Integer id = catId.intValue();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Category with id=%d not found", catId)
                ));
        return categoryMapper.toDto(category);
    }
}
