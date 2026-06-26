package ru.yandex.practicum.explore_with_me.feature.participationrequest.model;

public enum RequestStatus {
    PENDING,    // Ожидает модерации
    CONFIRMED,  // Подтвержден
    REJECTED,   // Отклонен
    CANCELED    // Отменен пользователем
}