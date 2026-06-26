package ru.yandex.practicum.explore_with_me.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.yandex.practicum.explore_with_me.config.Config;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(int id) {
        super(String.format(Config.NOT_FOUND_EXCEPTION, id));
    }
    public NotFoundException(long id) {
        super(String.format(Config.NOT_FOUND_EXCEPTION, id));
    }
}
