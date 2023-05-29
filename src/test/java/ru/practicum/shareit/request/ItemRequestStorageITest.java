package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserStorage;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestStorageITest {
    private final ItemRequestStorage itemRequestStorage;
    private final UserStorage userStorage;

    @AfterEach
    public void deleteUsers() {
        itemRequestStorage.deleteAll();
        userStorage.deleteAll();
    }

    @Test
    void findByRequestor_IdOrderByCreatedDesc_WhenRequestorIdNotExists_ThenReturnEmptyList() {
        User requestor = userStorage.save(new User(0L, "name", "mail@mail.ru"));
        itemRequestStorage.save(new ItemRequest("description1", requestor,
                LocalDateTime.now()));
        itemRequestStorage.save(new ItemRequest("description2", requestor,
                LocalDateTime.now()));

        assertThatCode(() -> {
            List<ItemRequest> result = itemRequestStorage.findByRequestor_IdOrderByCreatedDesc(requestor.getId() + 1);
            assertThat(result)
                    .as("Проверка возвращаемого значения, когда в базе нет запросов с таким id пользователя")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    void findByRequestor_IdOrderByCreatedDesc_WhenRequestorIdExists_ThenReturnNotEmptyList() {
        User requestor = userStorage.save(new User(0L, "name", "mail@mail.ru"));
        ItemRequest itemRequest1 = itemRequestStorage.save(new ItemRequest("description1", requestor,
                LocalDateTime.now().minusDays(1)));
        ItemRequest itemRequest2 = itemRequestStorage.save(new ItemRequest("description2", requestor,
                LocalDateTime.now().minusDays(2)));

        assertThatCode(() -> {
            List<ItemRequest> result = itemRequestStorage.findByRequestor_IdOrderByCreatedDesc(requestor.getId());
            assertThat(result)
                    .as("Проверка возвращаемого значения, когда в базе есть запросы с таким id пользователя")
                    .asList()
                    .hasSize(2)
                    .contains(itemRequest1, Index.atIndex(0))
                    .contains(itemRequest2, Index.atIndex(1));
        }).doesNotThrowAnyException();
    }

    @Test
    void findByRequestor_IdNot_WhenBookingsWithOnlyRequestorIdExist_ThenReturnEmptyPage() {
        User requestor = userStorage.save(new User(0L, "name", "mail@mail.ru"));
        itemRequestStorage.save(new ItemRequest("description1", requestor,
                LocalDateTime.now()));
        itemRequestStorage.save(new ItemRequest("description2", requestor,
                LocalDateTime.now()));
        Pageable page = PageRequest.of(0, 20);

        assertThatCode(() -> {
            Page<ItemRequest> result = itemRequestStorage.findByRequestor_IdNot(requestor.getId(), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда в базе нет запросов с другим id пользователя")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    void findByRequestor_IdNot_WhenBookingsWithNotRequestorIdExist_ThenReturnNotEmptyPage() {
        User requestor = userStorage.save(new User(0L, "name", "mail@mail.ru"));
        ItemRequest itemRequest1 = itemRequestStorage.save(new ItemRequest("description1", requestor,
                LocalDateTime.now().minusDays(1)));
        ItemRequest itemRequest2 = itemRequestStorage.save(new ItemRequest("description2", requestor,
                LocalDateTime.now().minusDays(2)));

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "created"));

            Page<ItemRequest> result = itemRequestStorage.findByRequestor_IdNot(requestor.getId() + 1, page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда есть сортировка и все записи вмещаются на одну" +
                            " страницу")
                    .asList()
                    .hasSize(2)
                    .contains(itemRequest1, Index.atIndex(0))
                    .contains(itemRequest2, Index.atIndex(1));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "created"));

            Page<ItemRequest> result = itemRequestStorage.findByRequestor_IdNot(requestor.getId() + 1, page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда записи не помещаются на одну страницу")
                    .asList()
                    .hasSize(1)
                    .contains(itemRequest1, Index.atIndex(0));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(1, 1, Sort.by(Sort.Direction.DESC, "created"));

            Page<ItemRequest> result = itemRequestStorage.findByRequestor_IdNot(requestor.getId() + 1, page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда записи не помещаются на одну страницу")
                    .asList()
                    .hasSize(1)
                    .contains(itemRequest2, Index.atIndex(0));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 20);

            Page<ItemRequest> result = itemRequestStorage.findByRequestor_IdNot(requestor.getId() + 1, page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда нет сортировки")
                    .asList()
                    .hasSize(2)
                    .contains(itemRequest1)
                    .contains(itemRequest2);
        }).doesNotThrowAnyException();
    }

}