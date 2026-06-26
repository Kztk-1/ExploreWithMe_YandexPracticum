package ru.yandex.practicum.explore_with_me.feature.category.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.explore_with_me.config.Config;
import ru.yandex.practicum.explore_with_me.exception.NotFoundException;
import ru.yandex.practicum.explore_with_me.exception.handler.ErrorResponse;
import ru.yandex.practicum.explore_with_me.exception.handler.ExceptionHandlerAbstract;
import ru.yandex.practicum.explore_with_me.feature.category.controller.AdminCategoryController;

@Order(1)
@RestControllerAdvice
public class CategoryExceptionHandler extends ExceptionHandlerAbstract {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(CategoryAlreadyUsed.class)
    public ResponseEntity<ErrorResponse> handleCategoryConflict(
            CategoryAlreadyUsed ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                Config.CATEGORY_ALREADY_USED_MESSAGE,
                request.getRequestURI()
        );
    }
}
