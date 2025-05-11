package ru.yandex.practicum.explore_with_me.feature.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateCategoryDto {
    @NotBlank
    @Size(min = 1, max = 50)
    String name;
}
