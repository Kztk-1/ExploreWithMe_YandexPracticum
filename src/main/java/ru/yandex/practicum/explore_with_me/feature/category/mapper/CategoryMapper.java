package ru.yandex.practicum.explore_with_me.feature.category.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.explore_with_me.feature.category.dto.CategoryDto;
import ru.yandex.practicum.explore_with_me.feature.category.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    /** Преобразует сущность Category в DTO */
    CategoryDto toDto(Category category);

    /** Преобразует DTO в сущность Category.
     *  Если вы хотите игнорировать какую-то логику заполнения полей,
     *  можно добавить аннотацию @Mapping с ignore = true.
     */
    Category toEntity(CategoryDto dto);

}
