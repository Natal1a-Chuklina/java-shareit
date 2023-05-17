package ru.practicum.shareit.utils;

public class Constants {
    private Constants() {
    }

    public static final String NOT_EMPTY_USER_NAME_MESSAGE = "Имя пользователя должно быть заполнено и не должно" +
            " быть пустым";
    public static final String NOT_EMPTY_EMAIL_MESSAGE = "Почта должна быть заполнена и не должна быть пустой";
    public static final String USER_ALREADY_EXISTS_MESSAGE = "Пользователь с почтой %s уже существует";
    public static final String USER_NOT_FOUND_MESSAGE = "Пользователь с идентификатором %d не найден";
    public static final String ITEM_NOT_FOUND_MESSAGE = "Вещь с идентификатором %d не найдена";
    public static final String USERS_ITEM_NOT_FOUND_MESSAGE = "Вещь с идентификатором %d не найдена у пользователя " +
            "с идентификатором %d";
    public static final String UNKNOWN_ERROR_MESSAGE = "Произошла неизвестная ошибка, попробуйте проверить корректность " +
            "всех данных запроса";
    public static final String WRONG_START_AND_END_BOOKING_DATES_MESSAGE = "Дата конца бронирования должна быть позже даты" +
            " начала бронирования";
    public static final String ITEM_NOT_AVAILABLE_MESSAGE = "Вещь с идентификатором %d не доступна к бронированию";
    public static final String BOOKING_NOT_FOUND_MESSAGE = "Бронирование с идентификатором %d не найдено";
    public static final String USER_CANNOT_BOOK_HIS_ITEM_MESSAGE = "Нельзя забронировать свою вещь";
    public static final String NOT_OWNER_CANNOT_CHANGE_BOOKING_STATUS_MESSAGE = "Изменить статус бронирования может" +
            " только владелец вещи";
    public static final String USER_CANNOT_CHANGE_BOOKING_STATUS_TWICE_MESSAGE = "Нельзя повторно изменить статус " +
            "бронирования";
    public static final String NOT_BOOKING_OR_ITEM_OWNER_CANNOT_GET_BOOKING_MESSAGE = "Посмотреть бронирование могут" +
            " только автор бронирования и владелец вещи";
    public static final String UNKNOWN_SEARCHING_STATE_MESSAGE = "Unknown state: %s";
    public static final String USER_CANNOT_LEAVE_COMMENT_MESSAGE = "Отзыв на вещь может оставить только тот пользователь, " +
            "который брал ее в аренду, и только после окончания срока аренды";
    public static final String USER_CANNOT_LEAVE_COMMENT_TWICE_MESSAGE = "Нельзя оставить больше одного отзыва на вещь";
    public static final String TIME_NOT_AVAILABLE_FOR_BOOKING_MESSAGE = "Нельзя забронировать вещь с %s по %s, так как " +
            "она уже забронирована на это время";
    public static final String HEADER_WITH_USER_ID_NAME = "X-Sharer-User-Id";
}
