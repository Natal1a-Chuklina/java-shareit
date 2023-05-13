package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserMapper;

import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.practicum.shareit.utils.Constants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;
    public static final int PAGE_SIZE = 20;
    public static final int START_PAGE = 0;

    @Override
    public List<UserDto> getAll() {
        List<UserDto> allUsers = new ArrayList<>();

        Sort sortById = Sort.by(Sort.Direction.ASC, "id");
        Pageable page = PageRequest.of(START_PAGE, PAGE_SIZE, sortById);

        do {
            Page<User> userPage = userStorage.findAll(page);
            userPage.getContent().forEach(user -> allUsers.add(UserMapper.toUserDto(user)));
            if (userPage.hasNext()) {
                page = PageRequest.of(userPage.getNumber() + 1, userPage.getSize(), userPage.getSort());
            } else {
                page = null;
            }
        } while (page != null);

        log.info("Получен список пользователей длиной {}", allUsers.size());
        return allUsers;
    }

    @Override
    public UserDto createUser(UserDto user) {
        user.setId(0);
        validateUser(user);

        try {
            User savedUser = userStorage.save(UserMapper.toUser(user));
            log.info("Создан пользователь с id = {}", savedUser.getId());
            return UserMapper.toUserDto(savedUser);
        } catch (DataIntegrityViolationException e) {
            log.warn("Выполнена попытка создать пользователя с почтой, которая уже есть в базе: {}", user.getEmail());
            throw new AlreadyExistException(String.format(USER_ALREADY_EXISTS_MESSAGE, user.getEmail()));
        }
    }

    @Override
    public UserDto getUser(long userId) {
        Optional<User> user = userStorage.findById(userId);

        if (user.isEmpty()) {
            log.warn("Выполнена попытка получить пользователя по несуществующему id = {}", userId);
            throw new NotFoundException(String.format(USER_NOT_FOUND_MESSAGE, userId));
        }

        log.info("Получен пользователь с id = {}", userId);
        return UserMapper.toUserDto(user.get());
    }

    @Override
    public UserDto updateUser(UserDto user) {
        Optional<User> currentUser = userStorage.findById(user.getId());

        if (currentUser.isEmpty()) {
            log.warn("Выполнена попытка обновить пользователя по несуществующему id = {}", user.getId());
            throw new NotFoundException(String.format(USER_NOT_FOUND_MESSAGE, user.getId()));
        }

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            currentUser.get().setEmail(user.getEmail());
        }

        if (user.getName() != null && !user.getName().isBlank()) {
            currentUser.get().setName(user.getName());
        }

        /* Не понимаю почему в лог выводится stack trace ошибки (DataIntegrityViolationException), хотя программа
         заходит в блок catch, т.е. это исключение отлавливается. Кроме того в методе createUser(UserDto user) строки
         57-64 практически идентичны строкам 101-108 этого метода, но там stack trace этой же ошибки не выводится. */

        try {
            User updatedUser = userStorage.save(currentUser.get());
            log.info("Обновлена информация о пользователе с id = {}", updatedUser.getId());
            return UserMapper.toUserDto(updatedUser);
        } catch (DataIntegrityViolationException e) {
            log.warn("Выполнена попытка установить пользователю почту, которая уже есть в базе: {}", user.getEmail());
            throw new AlreadyExistException(String.format(USER_ALREADY_EXISTS_MESSAGE, user.getEmail()));
        }
    }

    @Override
    public void deleteUser(long userId) {
        try {
            userStorage.deleteById(userId);
            log.info("Пользователь с id = {} удален", userId);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Выполнена попытка удалить несуществующего пользователя по id = {}", userId);
            throw new NotFoundException(String.format(USER_NOT_FOUND_MESSAGE, userId));
        }
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
