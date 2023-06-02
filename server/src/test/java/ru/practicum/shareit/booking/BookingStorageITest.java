package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.assertj.core.data.Index;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.ItemStorage;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserStorage;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingStorageITest {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final BookingStorage bookingStorage;

    @Test
    void findByBooker_Id_WhenBookingsWithBookerIdNotExist_ThenReturnEmptyPage() {
        User user1 = userStorage.save(new User(0L, "name1", "mail1@mail.ru"));
        User user2 = userStorage.save(new User(0L, "name2", "mail2@mail.ru"));
        Item item = itemStorage.save(new Item(0L, "name", "description", true, user1,
                null));
        bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3)));
        Pageable page = PageRequest.of(0, 20);

        assertThatCode(() -> {
            Page<Booking> result = bookingStorage.findByBooker_Id(user1.getId(), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда в базе нет бронирований с таким id владельца")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    void findByBooker_Id_WhenBookingsWithBookerIdExist_ThenReturnNotEmptyPage() {
        User user1 = userStorage.save(new User(0L, "name1", "mail1@mail.ru"));
        User user2 = userStorage.save(new User(0L, "name2", "mail2@mail.ru"));
        Item item = itemStorage.save(new Item(0L, "name", "description", true, user1,
                null));
        Booking booking1 = bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3)));
        Booking booking2 = bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(3)));

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByBooker_Id(user2.getId(), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения,когда есть сортировка и все записи вмещаются на " +
                            "одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(booking2, Index.atIndex(0))
                    .contains(booking1, Index.atIndex(1));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByBooker_Id(user2.getId(), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда записи не помещаются на одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(booking2, Index.atIndex(0));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(1, 1, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByBooker_Id(user2.getId(), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда записи не помещаются на одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(booking1, Index.atIndex(0));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 20);
            Page<Booking> result = bookingStorage.findByBooker_Id(user2.getId(), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда нет сортировки")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(booking2)
                    .contains(booking1);
        }).doesNotThrowAnyException();
    }

    @Test
    void findByBooker_IdAndEndBefore_WhenBookingsWithBookerIdAndEndBeforeNotExist_ThenReturnEmptyPage() {
        User user1 = userStorage.save(new User(0L, "name1", "mail1@mail.ru"));
        User user2 = userStorage.save(new User(0L, "name2", "mail2@mail.ru"));
        Item item = itemStorage.save(new Item(0L, "name", "description", true, user1,
                null));
        bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3)));
        Pageable page = PageRequest.of(0, 20);

        assertThatCode(() -> {
            Page<Booking> result = bookingStorage.findByBooker_IdAndEndBefore(user2.getId(), LocalDateTime.now(), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда в базе нет бронирований с таким id владельца " +
                            "и датой конца бронирования раньше переданной")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    void findByBooker_IdAndEndBefore_WhenBookingsWithBookerIdAndEndBeforeExist_ThenReturnNotEmptyPage() {
        User user1 = userStorage.save(new User(0L, "name1", "mail1@mail.ru"));
        User user2 = userStorage.save(new User(0L, "name2", "mail2@mail.ru"));
        Item item = itemStorage.save(new Item(0L, "name", "description", true, user1,
                null));
        Booking booking1 = bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3)));
        Booking booking2 = bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(3)));

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByBooker_IdAndEndBefore(user2.getId(),
                    LocalDateTime.now().plusDays(5), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения,когда есть сортировка и все записи вмещаются на " +
                            "одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(booking2, Index.atIndex(0))
                    .contains(booking1, Index.atIndex(1));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByBooker_IdAndEndBefore(user2.getId(),
                    LocalDateTime.now().plusDays(5), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда записи не помещаются на одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(booking2, Index.atIndex(0));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(1, 1, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByBooker_IdAndEndBefore(user2.getId(),
                    LocalDateTime.now().plusDays(5), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда записи не помещаются на одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(booking1, Index.atIndex(0));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 20);
            Page<Booking> result = bookingStorage.findByBooker_IdAndEndBefore(user2.getId(),
                    LocalDateTime.now().plusDays(5), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда нет сортировки")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(booking2)
                    .contains(booking1);
        }).doesNotThrowAnyException();
    }

    @Test
    void findByBooker_IdAndStartAfter_WhenBookingsWithBookerIdAndStartAfterNotExist_ThenReturnEmptyPage() {
        User user1 = userStorage.save(new User(0L, "name1", "mail1@mail.ru"));
        User user2 = userStorage.save(new User(0L, "name2", "mail2@mail.ru"));
        Item item = itemStorage.save(new Item(0L, "name", "description", true, user1,
                null));
        bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3)));
        Pageable page = PageRequest.of(0, 20);

        assertThatCode(() -> {
            Page<Booking> result = bookingStorage.findByBooker_IdAndStartAfter(user2.getId(),
                    LocalDateTime.now().plusDays(1), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда в базе нет бронирований с таким id владельца " +
                            "и датой начала бронирования позже переданной")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    void findByBooker_IdAndStartAfter_WhenBookingsWithBookerIdAndStartAfterExist_ThenReturnNotEmptyPage() {
        User user1 = userStorage.save(new User(0L, "name1", "mail1@mail.ru"));
        User user2 = userStorage.save(new User(0L, "name2", "mail2@mail.ru"));
        Item item = itemStorage.save(new Item(0L, "name", "description", true, user1,
                null));
        Booking booking1 = bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3)));
        Booking booking2 = bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(3)));

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByBooker_IdAndStartAfter(user2.getId(), LocalDateTime.now(), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения,когда есть сортировка и все записи вмещаются на " +
                            "одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(booking2, Index.atIndex(0))
                    .contains(booking1, Index.atIndex(1));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByBooker_IdAndStartAfter(user2.getId(), LocalDateTime.now(), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда записи не помещаются на одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(booking2, Index.atIndex(0));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(1, 1, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByBooker_IdAndStartAfter(user2.getId(), LocalDateTime.now(), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда записи не помещаются на одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(booking1, Index.atIndex(0));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 20);
            Page<Booking> result = bookingStorage.findByBooker_IdAndStartAfter(user2.getId(), LocalDateTime.now(), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда нет сортировки")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(booking2)
                    .contains(booking1);
        }).doesNotThrowAnyException();
    }

    @Test
    void findByBooker_IdAndStartBeforeAndEndAfter_WhenBookingsWithBookerIdAndStartBeforeAndEndAfterNotExist_ThenReturnEmptyPage() {
        User user1 = userStorage.save(new User(0L, "name1", "mail1@mail.ru"));
        User user2 = userStorage.save(new User(0L, "name2", "mail2@mail.ru"));
        Item item = itemStorage.save(new Item(0L, "name", "description", true, user1,
                null));
        bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3)));
        Pageable page = PageRequest.of(0, 20);

        assertThatCode(() -> {
            Page<Booking> result = bookingStorage.findByBooker_IdAndStartBeforeAndEndAfter(user2.getId(),
                    LocalDateTime.now(), LocalDateTime.now(), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда в базе нет бронирований с таким id владельца " +
                            "и переданной датой между началом и концом бронирования")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    void findByBooker_IdAndStartBeforeAndEndAfter_WhenBookingsWithBookerIdAndStartBeforeAndEndAfterExist_ThenReturnNotEmptyPage() {
        User user1 = userStorage.save(new User(0L, "name1", "mail1@mail.ru"));
        User user2 = userStorage.save(new User(0L, "name2", "mail2@mail.ru"));
        Item item = itemStorage.save(new Item(0L, "name", "description", true, user1,
                null));
        Booking booking1 = bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(4)));
        Booking booking2 = bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusDays(4)));

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByBooker_IdAndStartBeforeAndEndAfter(user2.getId(),
                    LocalDateTime.now().plusHours(3), LocalDateTime.now().plusHours(3), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения,когда есть сортировка и все записи вмещаются на " +
                            "одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(booking2, Index.atIndex(0))
                    .contains(booking1, Index.atIndex(1));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByBooker_IdAndStartBeforeAndEndAfter(user2.getId(),
                    LocalDateTime.now().plusHours(3), LocalDateTime.now().plusHours(3), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда записи не помещаются на одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(booking2, Index.atIndex(0));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(1, 1, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByBooker_IdAndStartBeforeAndEndAfter(user2.getId(),
                    LocalDateTime.now().plusHours(3), LocalDateTime.now().plusHours(3), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда записи не помещаются на одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(booking1, Index.atIndex(0));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 20);
            Page<Booking> result = bookingStorage.findByBooker_IdAndStartBeforeAndEndAfter(user2.getId(),
                    LocalDateTime.now().plusHours(3), LocalDateTime.now().plusHours(3), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда нет сортировки")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(booking2)
                    .contains(booking1);
        }).doesNotThrowAnyException();
    }

    @Test
    void findByBooker_IdAndStatus_WhenBookingsWithBookerIdAndStatusNotExist_ThenReturnEmptyPage() {
        User user1 = userStorage.save(new User(0L, "name1", "mail1@mail.ru"));
        User user2 = userStorage.save(new User(0L, "name2", "mail2@mail.ru"));
        Item item = itemStorage.save(new Item(0L, "name", "description", true, user1,
                null));
        bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3)));
        Pageable page = PageRequest.of(0, 20);

        assertThatCode(() -> {
            Page<Booking> result = bookingStorage.findByBooker_IdAndStatus(user2.getId(), Status.APPROVED, page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда в базе нет бронирований с таким id владельца " +
                            "и статусом")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    void findByBooker_IdAndStatus_WhenBookingsWithBookerIdAndStatusExist_ThenReturnNotEmptyPage() {
        User user1 = userStorage.save(new User(0L, "name1", "mail1@mail.ru"));
        User user2 = userStorage.save(new User(0L, "name2", "mail2@mail.ru"));
        Item item = itemStorage.save(new Item(0L, "name", "description", true, user1,
                null));
        Booking booking1 = bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3)));
        Booking booking2 = bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(3)));

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByBooker_IdAndStatus(user2.getId(), Status.WAITING, page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения,когда есть сортировка и все записи вмещаются на " +
                            "одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(booking2, Index.atIndex(0))
                    .contains(booking1, Index.atIndex(1));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByBooker_IdAndStatus(user2.getId(), Status.WAITING, page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда записи не помещаются на одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(booking2, Index.atIndex(0));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(1, 1, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByBooker_IdAndStatus(user2.getId(), Status.WAITING, page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда записи не помещаются на одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(booking1, Index.atIndex(0));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 20);
            Page<Booking> result = bookingStorage.findByBooker_IdAndStatus(user2.getId(), Status.WAITING, page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда нет сортировки")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(booking2)
                    .contains(booking1);
        }).doesNotThrowAnyException();
    }

    @Test
    void findByItem_User_Id_WhenBookingsWithItemUserIdNotExist_ThenReturnEmptyPage() {
        User user1 = userStorage.save(new User(0L, "name1", "mail1@mail.ru"));
        User user2 = userStorage.save(new User(0L, "name2", "mail2@mail.ru"));
        Item item = itemStorage.save(new Item(0L, "name", "description", true, user1,
                null));
        bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3)));
        Pageable page = PageRequest.of(0, 20);

        assertThatCode(() -> {
            Page<Booking> result = bookingStorage.findByItem_User_Id(user2.getId(), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда в базе нет бронирований с таким id владельца" +
                            " вещи")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    void findByItem_User_Id_WhenBookingsWithItemUserIdExist_ThenReturnNotEmptyPage() {
        User user1 = userStorage.save(new User(0L, "name1", "mail1@mail.ru"));
        User user2 = userStorage.save(new User(0L, "name2", "mail2@mail.ru"));
        Item item = itemStorage.save(new Item(0L, "name", "description", true, user1,
                null));
        Booking booking1 = bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3)));
        Booking booking2 = bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(3)));

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByItem_User_Id(user1.getId(), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения,когда есть сортировка и все записи вмещаются на " +
                            "одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(booking2, Index.atIndex(0))
                    .contains(booking1, Index.atIndex(1));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByItem_User_Id(user1.getId(), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда записи не помещаются на одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(booking2, Index.atIndex(0));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(1, 1, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByItem_User_Id(user1.getId(), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда записи не помещаются на одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(booking1, Index.atIndex(0));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 20);
            Page<Booking> result = bookingStorage.findByItem_User_Id(user1.getId(), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда нет сортировки")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(booking2)
                    .contains(booking1);
        }).doesNotThrowAnyException();
    }

    @Test
    void findByItem_User_IdAndStatus_WhenBookingsWithItemUserIdAndStatusNotExist_ThenReturnEmptyPage() {
        User user1 = userStorage.save(new User(0L, "name1", "mail1@mail.ru"));
        User user2 = userStorage.save(new User(0L, "name2", "mail2@mail.ru"));
        Item item = itemStorage.save(new Item(0L, "name", "description", true, user1,
                null));
        bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3)));
        Pageable page = PageRequest.of(0, 20);

        assertThatCode(() -> {
            Page<Booking> result = bookingStorage.findByItem_User_IdAndStatus(user1.getId(), Status.APPROVED, page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда в базе нет бронирований с таким id владельца" +
                            " вещи и статусом")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    void findByItem_User_IdAndStatus_WhenBookingsWithItemUserIdAndStatusExist_ThenReturnNotEmptyPage() {
        User user1 = userStorage.save(new User(0L, "name1", "mail1@mail.ru"));
        User user2 = userStorage.save(new User(0L, "name2", "mail2@mail.ru"));
        Item item = itemStorage.save(new Item(0L, "name", "description", true, user1,
                null));
        Booking booking1 = bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3)));
        Booking booking2 = bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(3)));

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByItem_User_IdAndStatus(user1.getId(), Status.WAITING, page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения,когда есть сортировка и все записи вмещаются на " +
                            "одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(booking2, Index.atIndex(0))
                    .contains(booking1, Index.atIndex(1));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByItem_User_IdAndStatus(user1.getId(), Status.WAITING, page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда записи не помещаются на одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(booking2, Index.atIndex(0));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(1, 1, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByItem_User_IdAndStatus(user1.getId(), Status.WAITING, page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда записи не помещаются на одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(booking1, Index.atIndex(0));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 20);
            Page<Booking> result = bookingStorage.findByItem_User_IdAndStatus(user1.getId(), Status.WAITING, page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда нет сортировки")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(booking2)
                    .contains(booking1);
        }).doesNotThrowAnyException();
    }

    @Test
    void findByItem_User_IdAndStartAfter_WhenBookingsWithItemUserIdAndStartAfterNotExist_ThenReturnEmptyPage() {
        User user1 = userStorage.save(new User(0L, "name1", "mail1@mail.ru"));
        User user2 = userStorage.save(new User(0L, "name2", "mail2@mail.ru"));
        Item item = itemStorage.save(new Item(0L, "name", "description", true, user1,
                null));
        bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3)));
        Pageable page = PageRequest.of(0, 20);

        assertThatCode(() -> {
            Page<Booking> result = bookingStorage.findByItem_User_IdAndStartAfter(user1.getId(),
                    LocalDateTime.now().plusDays(1), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда в базе нет бронирований с таким id владельца " +
                            "вещи и датой начала бронирования позже переданной")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    void findByItem_User_IdAndStartAfter_WhenBookingsWithItemUserIdAndStartAfterExist_ThenReturnNotEmptyPage() {
        User user1 = userStorage.save(new User(0L, "name1", "mail1@mail.ru"));
        User user2 = userStorage.save(new User(0L, "name2", "mail2@mail.ru"));
        Item item = itemStorage.save(new Item(0L, "name", "description", true, user1,
                null));
        Booking booking1 = bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3)));
        Booking booking2 = bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(3)));

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByItem_User_IdAndStartAfter(user1.getId(), LocalDateTime.now(), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения,когда есть сортировка и все записи вмещаются на " +
                            "одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(booking2, Index.atIndex(0))
                    .contains(booking1, Index.atIndex(1));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByItem_User_IdAndStartAfter(user1.getId(), LocalDateTime.now(), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда записи не помещаются на одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(booking2, Index.atIndex(0));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(1, 1, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByItem_User_IdAndStartAfter(user1.getId(), LocalDateTime.now(), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда записи не помещаются на одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(booking1, Index.atIndex(0));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 20);
            Page<Booking> result = bookingStorage.findByItem_User_IdAndStartAfter(user1.getId(), LocalDateTime.now(), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда нет сортировки")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(booking2)
                    .contains(booking1);
        }).doesNotThrowAnyException();
    }

    @Test
    void findByItem_User_IdAndEndBefore_WhenBookingsWithItemUserIdAndEndBeforeNotExist_ThenReturnEmptyPage() {
        User user1 = userStorage.save(new User(0L, "name1", "mail1@mail.ru"));
        User user2 = userStorage.save(new User(0L, "name2", "mail2@mail.ru"));
        Item item = itemStorage.save(new Item(0L, "name", "description", true, user1,
                null));
        bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3)));
        Pageable page = PageRequest.of(0, 20);

        assertThatCode(() -> {
            Page<Booking> result = bookingStorage.findByItem_User_IdAndEndBefore(user1.getId(), LocalDateTime.now(), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда в базе нет бронирований с таким id владельца " +
                            "вещи и датой конца бронирования раньше переданной")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    void findByItem_User_IdAndEndBefore_WhenBookingsWithItemUserIdAndEndBeforeExist_ThenReturnNotEmptyPage() {
        User user1 = userStorage.save(new User(0L, "name1", "mail1@mail.ru"));
        User user2 = userStorage.save(new User(0L, "name2", "mail2@mail.ru"));
        Item item = itemStorage.save(new Item(0L, "name", "description", true, user1,
                null));
        Booking booking1 = bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3)));
        Booking booking2 = bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(3)));

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByItem_User_IdAndEndBefore(user1.getId(),
                    LocalDateTime.now().plusDays(5), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения,когда есть сортировка и все записи вмещаются на " +
                            "одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(booking2, Index.atIndex(0))
                    .contains(booking1, Index.atIndex(1));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByItem_User_IdAndEndBefore(user1.getId(),
                    LocalDateTime.now().plusDays(5), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда записи не помещаются на одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(booking2, Index.atIndex(0));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(1, 1, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByItem_User_IdAndEndBefore(user1.getId(),
                    LocalDateTime.now().plusDays(5), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда записи не помещаются на одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(booking1, Index.atIndex(0));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 20);
            Page<Booking> result = bookingStorage.findByItem_User_IdAndEndBefore(user1.getId(),
                    LocalDateTime.now().plusDays(5), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда нет сортировки")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(booking2)
                    .contains(booking1);
        }).doesNotThrowAnyException();
    }

    @Test
    void findByItem_User_IdAndStartBeforeAndEndAfter_WhenBookingsWithItemUserIdAndStartBeforeAndEndAfterNotExist_ThenReturnEmptyPage() {
        User user1 = userStorage.save(new User(0L, "name1", "mail1@mail.ru"));
        User user2 = userStorage.save(new User(0L, "name2", "mail2@mail.ru"));
        Item item = itemStorage.save(new Item(0L, "name", "description", true, user1,
                null));
        bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3)));
        Pageable page = PageRequest.of(0, 20);

        assertThatCode(() -> {
            Page<Booking> result = bookingStorage.findByItem_User_IdAndStartBeforeAndEndAfter(user1.getId(),
                    LocalDateTime.now(), LocalDateTime.now(), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда в базе нет бронирований с таким id владельца " +
                            "и переданной датой между началом и концом бронирования")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    void findByItem_User_IdAndStartBeforeAndEndAfter_WhenBookingsWithItemUserIdAndStartBeforeAndEndAfterExist_ThenReturnNotEmptyPage() {
        User user1 = userStorage.save(new User(0L, "name1", "mail1@mail.ru"));
        User user2 = userStorage.save(new User(0L, "name2", "mail2@mail.ru"));
        Item item = itemStorage.save(new Item(0L, "name", "description", true, user1,
                null));
        Booking booking1 = bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(4)));
        Booking booking2 = bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusDays(4)));

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByItem_User_IdAndStartBeforeAndEndAfter(user1.getId(),
                    LocalDateTime.now().plusHours(3), LocalDateTime.now().plusHours(3), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения,когда есть сортировка и все записи вмещаются на " +
                            "одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(booking2, Index.atIndex(0))
                    .contains(booking1, Index.atIndex(1));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByItem_User_IdAndStartBeforeAndEndAfter(user1.getId(),
                    LocalDateTime.now().plusHours(3), LocalDateTime.now().plusHours(3), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда записи не помещаются на одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(booking2, Index.atIndex(0));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(1, 1, Sort.by(Sort.Direction.DESC, "start"));
            Page<Booking> result = bookingStorage.findByItem_User_IdAndStartBeforeAndEndAfter(user1.getId(),
                    LocalDateTime.now().plusHours(3), LocalDateTime.now().plusHours(3), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда записи не помещаются на одну страницу")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(booking1, Index.atIndex(0));
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Pageable page = PageRequest.of(0, 20);
            Page<Booking> result = bookingStorage.findByItem_User_IdAndStartBeforeAndEndAfter(user1.getId(),
                    LocalDateTime.now().plusHours(3), LocalDateTime.now().plusHours(3), page);
            assertThat(result.getContent())
                    .as("Проверка возвращаемого значения, когда нет сортировки")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(booking2)
                    .contains(booking1);
        }).doesNotThrowAnyException();
    }

    @Test
    void findByItem_IdAndItem_User_Id_WhenNoBookingsFound_ThenReturnEmptyList() {
        User user1 = userStorage.save(new User(0L, "name1", "mail1@mail.ru"));
        User user2 = userStorage.save(new User(0L, "name2", "mail2@mail.ru"));
        Item item1 = itemStorage.save(new Item(0L, "name1", "description1", true, user1,
                null));
        Item item2 = itemStorage.save(new Item(0L, "name2", "description2", true, user2,
                null));
        bookingStorage.save(new Booking(Status.WAITING, item1, user2, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(4)));
        bookingStorage.save(new Booking(Status.APPROVED, item2, user1, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)));

        assertThatCode(() -> {
            List<Booking> result = bookingStorage.findByItem_IdAndItem_User_IdAndStatusOrderByStartAsc(item2.getId(),
                    user1.getId(), Status.APPROVED);
            assertThat(result)
                    .as("Проверка возвращаемого значения, когда совпадают все параметры, кроме id владельца" +
                            " вещи")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            List<Booking> result = bookingStorage.findByItem_IdAndItem_User_IdAndStatusOrderByStartAsc(item2.getId(),
                    user2.getId(), Status.WAITING);
            assertThat(result)
                    .as("Проверка возвращаемого значения, когда совпадают все параметры, кроме статуса " +
                            "бронирования")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            List<Booking> result = bookingStorage.findByItem_IdAndItem_User_IdAndStatusOrderByStartAsc(item2.getId(),
                    user1.getId(), Status.WAITING);
            assertThat(result)
                    .as("Проверка возвращаемого значения, когда совпадают все параметры, кроме id вещи")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    void findByItem_IdAndItem_User_Id_WhenBookingsFound_ThenReturnNotEmptyList() {
        User user1 = userStorage.save(new User(0L, "name1", "mail1@mail.ru"));
        User user2 = userStorage.save(new User(0L, "name2", "mail2@mail.ru"));
        Item item = itemStorage.save(new Item(0L, "name", "description", true, user1,
                null));
        Booking booking1 = bookingStorage.save(new Booking(Status.APPROVED, item, user2, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(4)));
        Booking booking2 = bookingStorage.save(new Booking(Status.APPROVED, item, user2, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)));

        assertThatCode(() -> {
            List<Booking> result = bookingStorage.findByItem_IdAndItem_User_IdAndStatusOrderByStartAsc(item.getId(),
                    user1.getId(), Status.APPROVED);
            assertThat(result)
                    .as("Проверка возвращаемого значения, когда совпадают все параметры поиска")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(booking1, Index.atIndex(0))
                    .contains(booking2, Index.atIndex(1));
        }).doesNotThrowAnyException();
    }

    @Test
    void existsByItemId_WhenBookingNotFound_ThenReturnFalse() {
        User user1 = userStorage.save(new User(0L, "name1", "mail1@mail.ru"));
        User user2 = userStorage.save(new User(0L, "name2", "mail2@mail.ru"));
        Item item1 = itemStorage.save(new Item(0L, "name1", "description1", true, user1,
                null));
        Item item2 = itemStorage.save(new Item(0L, "name2", "description2", true, user2,
                null));
        bookingStorage.save(new Booking(Status.WAITING, item1, user2, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(4)));
        bookingStorage.save(new Booking(Status.APPROVED, item2, user1, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)));

        assertThatCode(() -> {
            boolean result = bookingStorage.existsByItem_IdAndBooker_IdAndStatusAndEndBefore(item2.getId(),
                    user1.getId(), Status.APPROVED, LocalDateTime.now());
            assertThat(result)
                    .as("Проверка возвращаемого значения, когда совпадают все параметры, кроме времени " +
                            "конца бронирования вещи")
                    .isFalse();
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            boolean result = bookingStorage.existsByItem_IdAndBooker_IdAndStatusAndEndBefore(item1.getId(),
                    user2.getId(), Status.APPROVED, LocalDateTime.now().plusHours(5));
            assertThat(result)
                    .as("Проверка возвращаемого значения, когда совпадают все параметры, кроме статуса " +
                            "бронирования вещи")
                    .isFalse();
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            boolean result = bookingStorage.existsByItem_IdAndBooker_IdAndStatusAndEndBefore(item2.getId(),
                    user2.getId(), Status.WAITING, LocalDateTime.now().plusHours(5));
            assertThat(result)
                    .as("Проверка возвращаемого значения, когда совпадают все параметры, кроме id вещи")
                    .isFalse();
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            boolean result = bookingStorage.existsByItem_IdAndBooker_IdAndStatusAndEndBefore(item1.getId(),
                    user1.getId(), Status.WAITING, LocalDateTime.now().plusHours(5));
            assertThat(result)
                    .as("Проверка возвращаемого значения, когда совпадают все параметры, кроме id владельца " +
                            "бронирования")
                    .isFalse();
        }).doesNotThrowAnyException();
    }

    @Test
    void existsByItemId_WhenBookingFound_ThenReturnTrue() {
        User user1 = userStorage.save(new User(0L, "name1", "mail1@mail.ru"));
        User user2 = userStorage.save(new User(0L, "name2", "mail2@mail.ru"));
        Item item = itemStorage.save(new Item(0L, "name", "description", true, user1,
                null));
        bookingStorage.save(new Booking(Status.WAITING, item, user2, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(4)));


        assertThatCode(() -> {
            boolean result = bookingStorage.existsByItem_IdAndBooker_IdAndStatusAndEndBefore(item.getId(),
                    user2.getId(), Status.WAITING, LocalDateTime.now().plusHours(5));
            assertThat(result)
                    .as("Проверка возвращаемого значения, когда совпадают все параметры, кроме id владельца " +
                            "бронирования")
                    .isTrue();
        }).doesNotThrowAnyException();
    }

    @Test
    void findByItem_IdAndEndAfterAndStatus_WhenBookingsNotFound_ThenReturnEmptyList() {
        User user1 = userStorage.save(new User(0L, "name1", "mail1@mail.ru"));
        User user2 = userStorage.save(new User(0L, "name2", "mail2@mail.ru"));
        Item item1 = itemStorage.save(new Item(0L, "name1", "description1", true, user1,
                null));
        Item item2 = itemStorage.save(new Item(0L, "name2", "description2", true, user2,
                null));
        bookingStorage.save(new Booking(Status.WAITING, item1, user2, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(4)));
        bookingStorage.save(new Booking(Status.APPROVED, item2, user1, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)));

        assertThatCode(() -> {
            List<Booking> result = bookingStorage.findByItem_IdAndEndAfterAndStatusOrderByStartAsc(item1.getId(),
                    LocalDateTime.now(), Status.APPROVED);
            assertThat(result)
                    .as("Проверка возвращаемого значения, когда совпадают все параметры, кроме статуса" +
                            " бронирования")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            List<Booking> result = bookingStorage.findByItem_IdAndEndAfterAndStatusOrderByStartAsc(item2.getId(),
                    LocalDateTime.now().plusDays(3), Status.APPROVED);
            assertThat(result)
                    .as("Проверка возвращаемого значения, когда совпадают все параметры, кроме времени конца " +
                            "бронирования")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            List<Booking> result = bookingStorage.findByItem_IdAndEndAfterAndStatusOrderByStartAsc(item1.getId(),
                    LocalDateTime.now(), Status.APPROVED);
            assertThat(result)
                    .as("Проверка возвращаемого значения, когда совпадают все параметры, кроме id вещи")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    void findByItem_IdAndEndAfterAndStatus_WhenBookingsFound_ThenReturnNotEmptyList() {
        User user1 = userStorage.save(new User(0L, "name1", "mail1@mail.ru"));
        User user2 = userStorage.save(new User(0L, "name2", "mail2@mail.ru"));
        Item item = itemStorage.save(new Item(0L, "name", "description", true, user1,
                null));
        Booking booking1 = bookingStorage.save(new Booking(Status.APPROVED, item, user2, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(4)));
        Booking booking2 = bookingStorage.save(new Booking(Status.APPROVED, item, user2, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)));

        assertThatCode(() -> {
            List<Booking> result = bookingStorage.findByItem_IdAndEndAfterAndStatusOrderByStartAsc(item.getId(),
                    LocalDateTime.now(), Status.APPROVED);
            assertThat(result)
                    .as("Проверка возвращаемого значения, когда совпадают все параметры поиска")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(booking1, Index.atIndex(0))
                    .contains(booking2, Index.atIndex(1));
        }).doesNotThrowAnyException();
    }
}