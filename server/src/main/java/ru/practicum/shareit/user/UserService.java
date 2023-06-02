package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getAll();

    UserDto createUser(UserDto user);

    UserDto getUser(long userId);

    UserDto updateUser(UserDto user);

    void deleteUser(long userId);
}
