package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.assertj.core.data.Index;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestStorage;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserStorage;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemStorageITest {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final ItemRequestStorage itemRequestStorage;

    @AfterEach
    public void deleteUsers() {
        itemStorage.deleteAll();
        userStorage.deleteAll();
        itemRequestStorage.deleteAll();
    }

    @Test
    void findByUser_Id_WhenItemsWithUserIdNotExist_ThenReturnEmptyPage() {
        User user = userStorage.save(new User(0L, "name", "mail@mail.ru"));
        itemStorage.save(new Item(0L, "name1", "description1", true, user,
                null));
        itemStorage.save(new Item(0L, "name2", "description2", true, user,
                null));
        Pageable page = PageRequest.of(0, 20);

        assertThatCode(() -> {
            Page<Item> result = itemStorage.findByUser_Id(user.getId() + 1, page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда в базе нет вещей с таким id владельца")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    void findByUser_Id_WhenItemsWithUserIdExist_ThenReturnNotEmptyPage() {
        User user = userStorage.save(new User(0L, "name", "mail@mail.ru"));
        Item item1 = itemStorage.save(new Item(0L, "name1", "description1", true, user,
                null));
        Item item2 = itemStorage.save(new Item(0L, "name2", "description2", true, user,
                null));

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id"));

            Page<Item> result = itemStorage.findByUser_Id(user.getId(), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения,когда есть сортировка и все записи вмещаются на " +
                            "одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(item1, Index.atIndex(0))
                    .contains(item2, Index.atIndex(1));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "id"));

            Page<Item> result = itemStorage.findByUser_Id(user.getId(), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда записи не помещаются на одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(item1, Index.atIndex(0));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(1, 1, Sort.by(Sort.Direction.ASC, "id"));

            Page<Item> result = itemStorage.findByUser_Id(user.getId(), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда записи не помещаются на одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(item2, Index.atIndex(0));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 20);

            Page<Item> result = itemStorage.findByUser_Id(user.getId(), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда нет сортировки")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(item1)
                    .contains(item2);
        }).doesNotThrowAnyException();
    }

    @Test
    void findByDescription_WhenItemsWithSearchStringNotExistOrNotAvailable_ThenReturnEmptyPage() {
        User user = userStorage.save(new User(0L, "name", "mail@mail.ru"));
        itemStorage.save(new Item(0L, "name1", "description1", true, user,
                null));
        itemStorage.save(new Item(0L, "name2", "description2", true, user,
                null));
        itemStorage.save(new Item(0L, "name3", "description3", false, user,
                null));
        Pageable page = PageRequest.of(0, 20);

        assertThatCode(() -> {
            Page<Item> result = itemStorage.findByDescriptionContainingAndAvailableTrueOrNameContainingAndAvailableTrueAllIgnoreCase(
                    "3", "3", page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда в базе нет доступных вещей, содержащих " +
                            "искомую строку в описании или названии")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    void findByDescription_WhenItemsWithSearchStringExistAndAvailable_ThenReturnEmptyPage() {
        User user = userStorage.save(new User(0L, "name", "mail@mail.ru"));
        Item item1 = itemStorage.save(new Item(0L, "name1", "description1", true, user,
                null));
        Item item2 = itemStorage.save(new Item(0L, "name2", "description2", true, user,
                null));
        itemStorage.save(new Item(0L, "name3", "description3", false, user,
                null));

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id"));
            Page<Item> result = itemStorage.findByDescriptionContainingAndAvailableTrueOrNameContainingAndAvailableTrueAllIgnoreCase(
                    "descr", "descr", page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда есть сортировка и все записи вмещаются на " +
                            "одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(item1, Index.atIndex(0))
                    .contains(item2, Index.atIndex(1));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "id"));
            Page<Item> result = itemStorage.findByDescriptionContainingAndAvailableTrueOrNameContainingAndAvailableTrueAllIgnoreCase(
                    "descr", "descr", page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда записи не помещаются на одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(item1, Index.atIndex(0));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(1, 1, Sort.by(Sort.Direction.ASC, "id"));
            Page<Item> result = itemStorage.findByDescriptionContainingAndAvailableTrueOrNameContainingAndAvailableTrueAllIgnoreCase(
                    "descr", "descr", page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда записи не помещаются на одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(item2, Index.atIndex(0));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 20);
            Page<Item> result = itemStorage.findByDescriptionContainingAndAvailableTrueOrNameContainingAndAvailableTrueAllIgnoreCase(
                    "name", "name", page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда нет сортировки")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(item1)
                    .contains(item2);
        }).doesNotThrowAnyException();
    }

    @Test
    void existsByUser_IdAndId_WhenItemWithSuchIdAndUserIdExist_ThenReturnTrue() {
        User user = userStorage.save(new User(0L, "name", "mail@mail.ru"));
        Item item = itemStorage.save(new Item(0L, "name", "description", true, user,
                null));

        assertThatCode(() -> {
            boolean result = itemStorage.existsByUser_IdAndId(user.getId(), item.getId());

            assertThat(result)
                    .as("Проверка возвращаемого значения, когда в базе есть вещь с такими id вещи и " +
                            "пользователя")
                    .isTrue();
        }).doesNotThrowAnyException();
    }

    @Test
    void existsByUser_IdAndId_WhenItemWithSuchIdAndUserIdNotExist_ThenReturnTrue() {
        User user = userStorage.save(new User(0L, "name", "mail@mail.ru"));
        Item item = itemStorage.save(new Item(0L, "name", "description", true, user,
                null));

        assertThatCode(() -> {
            boolean result = itemStorage.existsByUser_IdAndId(user.getId() + 1, item.getId());

            assertThat(result)
                    .as("Проверка возвращаемого значения, когда в базе нет вещи с такими id вещи и " +
                            "пользователя")
                    .isFalse();
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            boolean result = itemStorage.existsByUser_IdAndId(user.getId(), item.getId() + 1);

            assertThat(result)
                    .as("Проверка возвращаемого значения, когда в базе нет вещи с такими id")
                    .isFalse();
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            boolean result = itemStorage.existsByUser_IdAndId(user.getId() + 1, item.getId() + 1);

            assertThat(result)
                    .as("Проверка возвращаемого значения, когда в базе есть вещь с такими id пользователя")
                    .isFalse();
        }).doesNotThrowAnyException();
    }

    @Test
    void findByItemRequest_Id_WhenItemsWithRequestsNotExist_ThenReturnEmptyList() {
        User user = userStorage.save(new User(0L, "name", "mail@mail.ru"));
        itemStorage.save(new Item(0L, "name1", "description1", true, user,
                null));
        itemStorage.save(new Item(0L, "name2", "description2", true, user,
                null));

        assertThatCode(() -> {
            List<Item> result = itemStorage.findByItemRequest_Id(1L);
            assertThat(result)
                    .as("Проверка возвращаемого значения, когда в базе нет вещей, созданных по запросам")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    void findByItemRequest_Id_WhenItemsWithRequestIdExist_ThenReturnEmptyList() {
        User user = userStorage.save(new User(0L, "name", "mail@mail.ru"));
        ItemRequest itemRequest = itemRequestStorage.save(new ItemRequest("description", user,
                LocalDateTime.now().minusHours(1)));
        Item item1 = itemStorage.save(new Item(0L, "name1", "description1", true, user,
                itemRequest));
        Item item2 = itemStorage.save(new Item(0L, "name2", "description2", true, user,
                itemRequest));

        assertThatCode(() -> {
            List<Item> result = itemStorage.findByItemRequest_Id(1L);
            assertThat(result)
                    .as("Проверка возвращаемого значения, когда в базе есть вещи по запросу с таким id")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(item1)
                    .contains(item2);
        }).doesNotThrowAnyException();
    }
}