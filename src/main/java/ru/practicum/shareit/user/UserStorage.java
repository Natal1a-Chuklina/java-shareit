package ru.practicum.shareit.user;

import java.util.List;

public interface UserStorage {
    User create(User user);

    User get(long userId);

    User update(User user);

    void delete(long userId);

    List<User> getAll();

    boolean isUserExist(long userId);
}
