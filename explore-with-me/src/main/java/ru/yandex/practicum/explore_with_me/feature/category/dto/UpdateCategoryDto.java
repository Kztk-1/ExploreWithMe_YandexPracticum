package ru.yandex.practicum.explore_with_me.feature.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCategoryDto {
    @NotBlank
    @Size(min = 1, max = 50)
    String name;
}
