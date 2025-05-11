package ru.yandex.practicum.explore_with_me.feature.category.dto;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

class NewCategoryDto {
    @NotBlank
    @Size(min = 1, max = 50)
    String name;
}
