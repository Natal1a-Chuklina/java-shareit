package ru.practicum.shareit.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.practicum.shareit.booking.dto.SearchingState;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static ru.practicum.shareit.utils.Constants.*;

class ValidatorTest {

    @ParameterizedTest
    @MethodSource("wrongNameUserDtosStream")
    void validateUser_WhenNameIsNullOrEmpty_ThenThrowsValidationException(UserDto userDto) {
        assertThatExceptionOfType(ValidationException.class)
                .as("Проверка валидации пользователя с пустым или null именем")
                .isThrownBy(() -> Validator.validateUser(userDto))
                .withMessage(NOT_EMPTY_USER_NAME_MESSAGE);
    }

    static Stream<UserDto> wrongNameUserDtosStream() {
        UserDto inputDto1 = new UserDto(0, " ", "email@mail.ru");
        UserDto inputDto2 = new UserDto(0, null, "email@mail.ru");
        UserDto inputDto3 = new UserDto(0, null, null);
        UserDto inputDto4 = new UserDto(0, "  ", "");

        return Stream.of(inputDto1, inputDto2, inputDto3, inputDto4);
    }

    @ParameterizedTest
    @MethodSource("wrongEmailUserDtosStream")
    void validateUser_WhenEmailIsNullOrEmpty_ThenThrowsValidationException(UserDto userDto) {
        assertThatExceptionOfType(ValidationException.class)
                .as("Проверка валидации пользователя с пустым или null email")
                .isThrownBy(() -> Validator.validateUser(userDto))
                .withMessage(NOT_EMPTY_EMAIL_MESSAGE);
    }

    static Stream<UserDto> wrongEmailUserDtosStream() {
        UserDto inputDto1 = new UserDto(0, "name", null);
        UserDto inputDto2 = new UserDto(0, "name", "");

        return Stream.of(inputDto1, inputDto2);
    }

    @Test
    void validateUser_WhenDtoIsCorrect_ThenDoNothing() {
        UserDto userDto = new UserDto(0, "name", "email@mail.ru");
        assertThatCode(() -> Validator.validateUser(userDto)).doesNotThrowAnyException();
    }

    @Test
    void checkTimeCorrectness_WhenTimeIsIncorrect_ThenThrowsIllegalArgumentException() {
        LocalDateTime start = LocalDateTime.now().plusHours(5);
        LocalDateTime end = LocalDateTime.now().plusHours(2);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .as("Проверка валидации времени, когда дата начала позже даты конца")
                .isThrownBy(() -> Validator.checkTimeCorrectness(start, end))
                .withMessage(WRONG_START_AND_END_BOOKING_DATES_MESSAGE);

        LocalDateTime time = LocalDateTime.now().plusHours(1);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .as("Проверка валидации времени, когда дата начала равна дате конца")
                .isThrownBy(() -> Validator.checkTimeCorrectness(time, time))
                .withMessage(WRONG_START_AND_END_BOOKING_DATES_MESSAGE);
    }

    @Test
    void checkTimeCorrectness_WhenTimeIsCorrect_ThenDoNothing() {
        assertThatCode(() -> Validator.checkTimeCorrectness(LocalDateTime.now(), LocalDateTime.now().plusHours(2)))
                .doesNotThrowAnyException();
    }

    @Test
    void getSearchingState_WhenStateIsIncorrect_ThenThrowsIllegalArgumentException() {
        String state = "wrong_state";

        assertThatExceptionOfType(IllegalArgumentException.class)
                .as("Проверка валидации поисковой строки")
                .isThrownBy(() -> Validator.getSearchingState(state))
                .withMessage(String.format(UNKNOWN_SEARCHING_STATE_MESSAGE, state));
    }

    @Test
    void getSearchingState_WhenStateIsCorrect_ThenReturnSearchingState() {
        String state = "PAST";

        assertThatCode(() -> {
            SearchingState parsedState = Validator.getSearchingState(state);

            assertThat(parsedState).isEqualTo(SearchingState.PAST);
        }).doesNotThrowAnyException();
    }
}