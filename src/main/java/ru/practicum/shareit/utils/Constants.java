package ru.practicum.shareit.utils;

public class Constants {
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
}
