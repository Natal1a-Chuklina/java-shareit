package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplITest {
    private final UserService userService;

    @Test
    void getAll_WhenDbIsEmpty_ThenReturnEmptyList() {
        assertThatCode(() -> {
            List<UserDto> users = userService.getAll();
            assertThat(users)
                    .as("Проверка получения пустого списка пользователей при пустой бд")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getAll_WhenDbIsNotEmpty_ThenReturnNotEmptyList() {
        UserDto user1 = userService.createUser(new UserDto(0, "name1", "mail1@mail.ru"));
        UserDto user2 = userService.createUser(new UserDto(0, "name2", "mail2@mail.ru"));

        assertThatCode(() -> {
            List<UserDto> users = userService.getAll();

            assertThat(users)
                    .as("Проверка получения непустого списка пользователей при непустой бд")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(user1)
                    .contains(user2);
        }).doesNotThrowAnyException();
    }
}