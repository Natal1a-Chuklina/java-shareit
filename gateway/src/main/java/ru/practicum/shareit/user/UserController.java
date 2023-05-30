package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.Positive;

import static ru.practicum.shareit.utils.Constants.NOT_EMPTY_EMAIL_MESSAGE;
import static ru.practicum.shareit.utils.Constants.NOT_EMPTY_USER_NAME_MESSAGE;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> createUser(@Valid @RequestBody UserDto user) {
        log.info("Попытка создать пользователя");
        validateUser(user);
        return userClient.createUser(user);
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        log.info("Попытка получить список всех пользователей");
        return userClient.getAllUsers();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUser(@PathVariable @Positive long userId) {
        log.info("Попытка получить пользователя с id = {}", userId);
        return userClient.getUser(userId);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(@PathVariable @Positive long userId) {
        log.info("Попытка удалить пользователя с id = {}", userId);
        return userClient.deleteUser(userId);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> updateUser(@PathVariable @Positive long userId, @Valid @RequestBody UserDto user) {
        log.info("Попытка обновить пользователя с id = {}", userId);
        return userClient.updateUser(user, userId);
    }

    private void validateUser(UserDto user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.warn("Выполнена попытка создать пользователя с некорректным именем: {}", user.getName());
            throw new ValidationException(NOT_EMPTY_USER_NAME_MESSAGE);
        }

        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            log.warn("Выполнена попытка создать пользователя с некорректной почтой: {}", user.getEmail());
            throw new ValidationException(NOT_EMPTY_EMAIL_MESSAGE);
        }
    }
}
