package ru.practicum.shareit.utils;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.booking.dto.SearchingState;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.ValidationException;
import java.time.LocalDateTime;

import static ru.practicum.shareit.utils.Constants.*;

@Slf4j
public class Validator {
    private Validator() {
    }

    public static void validateUser(UserDto user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.warn("Выполнена попытка создать пользователя с некорректным именем: {}", user.getName());
            throw new ValidationException(NOT_EMPTY_USER_NAME_MESSAGE);
        }

        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            log.warn("Выполнена попытка создать пользователя с некорректной почтой: {}", user.getEmail());
            throw new ValidationException(NOT_EMPTY_EMAIL_MESSAGE);
        }
    }

    public static void checkTimeCorrectness(LocalDateTime start, LocalDateTime end) {
        if (!start.isBefore(end)) {
            log.warn("Выполнена попытка создать бронирование с некорректными датами: start = {}, end = {}", start, end);
            throw new IllegalArgumentException(WRONG_START_AND_END_BOOKING_DATES_MESSAGE);
        }
    }

    public static SearchingState getSearchingState(String state) {
        try {
            return SearchingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            log.warn("Выполнена попытка получить список бронирований по несуществующему статусу {}", state);
            throw new IllegalArgumentException(String.format(UNKNOWN_SEARCHING_STATE_MESSAGE, state));
        }
    }
}
