package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserStorageITest {
    private final UserStorage userStorage;

    @AfterEach
    public void deleteUsers() {
        userStorage.deleteAll();
    }

    @Test
    void existsByEmailAndIdNot_WhenExisting_ThenReturnTrue() {
        User user1 = userStorage.save(new User(0L, "name1", "email1@mail.ru"));
        User user2 = userStorage.save(new User(0L, "name2", "email2@mail.ru"));

        assertThatCode(() -> {
            boolean result = userStorage.existsByEmailAndIdNot(user1.getEmail(), user2.getId());
            assertThat(result)
                    .as("Проверка возвращаемого значения, когда в базе есть другой пользователь с такой почтой")
                    .isTrue();
        }).doesNotThrowAnyException();
    }

    @Test
    void existsByEmailAndIdNot_WhenNotExisting_ThenReturnFalse() {
        User user1 = userStorage.save(new User(0L, "name1", "email1@mail.ru"));

        assertThatCode(() -> {
            boolean result = userStorage.existsByEmailAndIdNot(user1.getEmail(), user1.getId());
            assertThat(result)
                    .as("Проверка возвращаемого значения, когда в базе нет другого пользователя с такой почтой")
                    .isFalse();
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            boolean result = userStorage.existsByEmailAndIdNot("random@mail.ru", user1.getId());
            assertThat(result)
                    .as("Проверка возвращаемого значения, когда в базе нет такой почты")
                    .isFalse();
        }).doesNotThrowAnyException();
    }
}