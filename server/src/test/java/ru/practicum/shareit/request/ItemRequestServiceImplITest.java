package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.assertj.core.data.Index;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswersDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceImplITest {
    private final ItemRequestService requestService;
    private final UserService userService;

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getRequests_WhenDbIsEmpty_ThenReturnEmptyList() {
        UserDto user = userService.createUser(new UserDto(0, "name", "mail@mail.ru"));

        assertThatCode(() -> {
            List<ItemRequestWithAnswersDto> requests = requestService.getRequests(user.getId(), 0, 20);
            assertThat(requests)
                    .as("Проверка получения пустого списка запросов при пустой бд")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @SneakyThrows
    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getRequests_WhenDbIsNotEmpty_ThenReturnNotEmptyList() {
        UserDto user1 = userService.createUser(new UserDto(0, "name1", "mail1@mail.ru"));
        UserDto user2 = userService.createUser(new UserDto(0, "name2", "mail2@mail.ru"));
        ItemRequestDto request1 = requestService.createItemRequest(user1.getId(), new CreateItemRequestDto(
                "description1"));
        Thread.sleep(1000);
        ItemRequestDto request2 = requestService.createItemRequest(user1.getId(), new CreateItemRequestDto(
                "description2"));
        ItemRequestWithAnswersDto expectedDto2 = requestService.getRequestById(user2.getId(), request2.getId());
        ItemRequestWithAnswersDto expectedDto1 = requestService.getRequestById(user1.getId(), request1.getId());

        assertThatCode(() -> {
            List<ItemRequestWithAnswersDto> requests = requestService.getRequests(user2.getId(), 5, 5);
            assertThat(requests)
                    .as("Проверка получения пустого списка, когда на странице нет результатов")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            List<ItemRequestWithAnswersDto> requests = requestService.getRequests(user1.getId(), 0, 20);
            assertThat(requests)
                    .as("Проверка получения пустого списка, когда в базе есть только запросы от данного " +
                            "пользователя")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            List<ItemRequestWithAnswersDto> requests = requestService.getRequests(user2.getId(), 0, 2);
            assertThat(requests)
                    .as("Проверка получения не пустого списка запросов")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(expectedDto2, Index.atIndex(0))
                    .contains(expectedDto1, Index.atIndex(1));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            List<ItemRequestWithAnswersDto> requests = requestService.getRequests(user2.getId(), 0, 1);
            assertThat(requests)
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(expectedDto2, Index.atIndex(0));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            List<ItemRequestWithAnswersDto> requests = requestService.getRequests(user2.getId(), 1, 1);
            assertThat(requests)
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(expectedDto1, Index.atIndex(0));
        }).doesNotThrowAnyException();
    }


}