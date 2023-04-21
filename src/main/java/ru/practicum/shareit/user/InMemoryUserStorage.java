package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users;
    private long lastId;

    public InMemoryUserStorage() {
        users = new HashMap<>();
    }

    @Override
    public User create(User user) {
        user.setId(++lastId);
        users.put(user.getId(), user);

        log.info("Создан пользователь с id = {}", user.getId());
        return user;
    }

    @Override
    public User get(long userId) {
        User user = users.get(userId);
        log.info("Получен пользователь с id = {}", userId);

        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        log.info("Обновлена информация о пользователе с id = {}", user.getId());

        return user;
    }

    @Override
    public void delete(long userId) {
        users.remove(userId);
        log.info("Пользователь с id = {} удален", userId);
    }

    @Override
    public List<User> getAll() {
        List<User> allUsers = new ArrayList<>(users.values());
        log.info("Получен список пользователей длиной {}", allUsers.size());

        return allUsers;
    }

    @Override
    public boolean isUserExist(long userId) {
        log.info("Получена информация о существовании пользователя с id = {}", userId);
        return users.containsKey(userId);
    }
}
