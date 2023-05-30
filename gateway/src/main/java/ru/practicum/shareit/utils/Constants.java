package ru.practicum.shareit.utils;

public class Constants {
    private Constants() {
    }

    public static final String NOT_EMPTY_USER_NAME_MESSAGE = "Имя пользователя должно быть заполнено и не должно" +
            " быть пустым";
    public static final String NOT_EMPTY_EMAIL_MESSAGE = "Почта должна быть заполнена и не должна быть пустой";
    public static final String HEADER_WITH_USER_ID_NAME = "X-Sharer-User-Id";
    public static final String DEFAULT_PAGE_SIZE = "20";
    public static final String DEFAULT_START_PAGE = "0";
    public static final String WRONG_START_AND_END_BOOKING_DATES_MESSAGE = "Дата конца бронирования должна быть позже даты" +
            " начала бронирования";
    public static final String UNKNOWN_SEARCHING_STATE_MESSAGE = "Unknown state: %s";
    public static final String UNKNOWN_ERROR_MESSAGE = "Произошла неизвестная ошибка, попробуйте проверить корректность " +
            "всех данных запроса";
}
