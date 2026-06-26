package ru.yandex.practicum.explore_with_me.config;

public class Config {

    public static final String NOT_FOUND_EXCEPTION = "Object with id=%d not found";

    public static final int USER_NAME_MAX_LENGTH = 50;
    public static final int USER_EMAIL_MAX_LENGTH = 100;

    public static final String CONFLICT_EXCEPTION_MESSAGE = "Conflict";


    public static final String CATEGORY_NOTFOUND_EXCEPTION_MESSAGE = "Category with id=%d not found";
    public static final String CATEGORY_ALREADY_USED_MESSAGE = "Category with id=%d is already used";

    public static final String EVENT_NOTFOUND_EXCEPTION_MESSAGE = "Event with id=%d not found";
    public static final String EVENT_PUBLISH_CONFLICT_EXCEPTION_MESSAGE = "Cannot publish the event because it's not in the right state: PUBLISHED";

    public static final String REQUEST_DUPLICATE_EXCEPTION_MESSAGE = "Запрос на участие уже существует";
    public static final String REQUEST_OWN_EVENT_EXCEPTION_MESSAGE = "Инициатор события не может подать заявку на участие в своём событии";
    public static final String REQUEST_UNPUBLISHED_EVENT_EXCEPTION_MESSAGE = "Нельзя участвовать в неопубликованном событии";
    public static final String REQUEST_PARTICIPANT_LIMIT_EXCEPTION_MESSAGE = "Достигнут лимит участников события";
    public static final String REQUEST_NOT_PENDING_EXCEPTION_MESSAGE = "Нельзя изменить статус заявки, которая не находится в статусе PENDING";
}
