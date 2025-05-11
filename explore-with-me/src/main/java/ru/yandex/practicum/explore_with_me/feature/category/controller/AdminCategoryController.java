package ru.yandex.practicum.explore_with_me.feature.category.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.explore_with_me.feature.category.dto.CategoryDto;
import ru.yandex.practicum.explore_with_me.feature.category.dto.NewCategoryDto;
import ru.yandex.practicum.explore_with_me.feature.category.dto.UpdateCategoryDto;
import ru.yandex.practicum.explore_with_me.feature.category.service.CategoryService;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        return categoryService.addCategory(newCategoryDto);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        categoryService.deleteCategory(catId);
    }

    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(@PathVariable Long catId,
                                      @Valid @RequestBody UpdateCategoryDto updateDto) {
        return categoryService.updateCategory(catId, updateDto);
    }
}
