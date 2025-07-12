package ru.yandex.practicum.explore_with_me.feature.category.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.yandex.practicum.explore_with_me.config.Config;
import ru.yandex.practicum.explore_with_me.exception.ConflictException;

@ResponseStatus(HttpStatus.CONFLICT)
public class CategoryAlreadyUsed extends ConflictException {

    public CategoryAlreadyUsed(String message) {
        super(message);
    }

    public CategoryAlreadyUsed(Integer id) {
        super(String.format(Config.CATEGORY_ALREADY_USED_MESSAGE, id));
    }


}
