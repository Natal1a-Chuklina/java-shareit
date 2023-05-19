package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.utils.Constants;

import javax.validation.ValidationException;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.utils.Constants.USER_ALREADY_EXISTS_MESSAGE;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserStorage userStorage;

    @Test
    void createUser_whenUserDtoIsCorrect_thenUserCreated() {
        User userToSave = new User(0L, "name", "email@mail.ru");
        UserDto inputUserDto = new UserDto(11, "name", "email@mail.ru");
        UserDto expectedUserDto = new UserDto(1, "name", "email@mail.ru");
        when(userStorage.save(userToSave))
                .thenReturn(new User(1L, "name", "email@mail.ru"));


        assertThatCode(() -> {
            UserDto actuslUserDto = userService.createUser(inputUserDto);
            assertThat(actuslUserDto)
                    .as("Проверка создания пользователя при корректных входных данных")
                    .isNotNull()
                    .isEqualTo(expectedUserDto);
        }).doesNotThrowAnyException();

        verify(userStorage, Mockito.times(1)).save(userToSave);
    }

    @Test
    void createUser_whenUserDtoIsIncorrect_thenThrowValidationException() {
        UserDto inputDto1 = new UserDto(0, " ", "email@mail.ru");
        UserDto inputDto2 = new UserDto(0, null, "email@mail.ru");
        UserDto inputDto3 = new UserDto(0, "name", null);
        UserDto inputDto4 = new UserDto(0, "name", "");

        assertThatExceptionOfType(ValidationException.class)
                .as("Проверка добавления пользователя с пустым именем")
                .isThrownBy(() -> userService.createUser(inputDto1))
                .withMessage(Constants.NOT_EMPTY_USER_NAME_MESSAGE);

        assertThatExceptionOfType(ValidationException.class)
                .as("Проверка добавления пользователя с null именем")
                .isThrownBy(() -> userService.createUser(inputDto2))
                .withMessage(Constants.NOT_EMPTY_USER_NAME_MESSAGE);

        assertThatExceptionOfType(ValidationException.class)
                .as("Проверка добавления пользователя с null почтой")
                .isThrownBy(() -> userService.createUser(inputDto3))
                .withMessage(Constants.NOT_EMPTY_EMAIL_MESSAGE);

        assertThatExceptionOfType(ValidationException.class)
                .as("Проверка добавления пользователя с пустой почтой")
                .isThrownBy(() -> userService.createUser(inputDto4))
                .withMessage(Constants.NOT_EMPTY_EMAIL_MESSAGE);

        verify(userStorage, Mockito.never()).save(any(User.class));
    }

    @Test
    void createUser_whenUserEmailAlreadyExists_thenThrowAlreadyExistException() {
        User userToSave = new User(0L, "name", "email@mail.ru");
        when(userStorage.save(userToSave))
                .thenThrow(DataIntegrityViolationException.class);

        assertThatExceptionOfType(AlreadyExistException.class)
                .as("Проверка добавления пользователя с уже существующей почтой")
                .isThrownBy(() -> userService.createUser(new UserDto(0, "name", "email@mail.ru")))
                .withMessage(String.format(USER_ALREADY_EXISTS_MESSAGE, "email@mail.ru"));

        verify(userStorage, Mockito.times(1)).save(userToSave);
    }
}