package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.ValidationException;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.utils.Constants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public List<UserDto> getAll() {
        return userStorage.getAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto createUser(UserDto user) {
        validateUser(user);
        checkEmailUniqueness(user);

        return UserMapper.toUserDto(userStorage.create(UserMapper.toUser(user)));
    }

    @Override
    public UserDto getUser(long userId) {
        checkUserExistence(userId);

        return UserMapper.toUserDto(userStorage.get(userId));
    }

    @Override
    public UserDto updateUser(UserDto user) {
        checkUserExistence(user.getId());
        checkEmailUniqueness(user);

        User currentUser = userStorage.get(user.getId());

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            currentUser.setEmail(user.getEmail());
        }

        if (user.getName() != null && !user.getName().isBlank()) {
            currentUser.setName(user.getName());
        }

        return UserMapper.toUserDto(userStorage.update(currentUser));
    }

    @Override
    public void deleteUser(long userId) {
        checkUserExistence(userId);
        userStorage.delete(userId);
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

    private void checkEmailUniqueness(UserDto user) {
        long coincidencesCount = getAll().stream()
                .filter(userToCompare -> userToCompare.getEmail().equals(user.getEmail())
                        && userToCompare.getId() != user.getId())
                .count();

        if (coincidencesCount > 0) {
            log.warn("Выполнена попытка создать пользователя с почтой, которая уже есть в базе: {}", user.getEmail());
            throw new AlreadyExistException(String.format(USER_ALREADY_EXISTS_MESSAGE, user.getEmail()));
        }
    }

    private void checkUserExistence(long userId) {
        if (!userStorage.isUserExist(userId)) {
            log.warn("Выполнена попытка получить пользователя по несуществующему id = {}", userId);
            throw new NotFoundException(String.format(USER_NOT_FOUND_MESSAGE, userId));
        }
    }
}
