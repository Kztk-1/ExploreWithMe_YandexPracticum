package ru.yandex.practicum.explore_with_me.exception;

import ru.yandex.practicum.explore_with_me.config.Config;

public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException() {
        super(Config.CONFLICT_EXCEPTION_MESSAGE);
    }

}
