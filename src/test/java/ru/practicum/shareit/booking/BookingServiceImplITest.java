package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.assertj.core.data.Index;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplITest {
    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getBookingsByBookerId_WhenALLSearchingState_ThenReturnAllBookings() {
        UserDto user1 = userService.createUser(new UserDto(0, "user1", "user1@mail.ru"));
        UserDto user2 = userService.createUser(new UserDto(0, "user2", "user2@mail.ru"));
        UserDto user3 = userService.createUser(new UserDto(0, "user3", "user3@mail.ru"));
        ItemDto item1 = itemService.createItem(user1.getId(), new ItemDto(0, "item1",
                "item1 of user1", true, null));
        ItemDto item2 = itemService.createItem(user1.getId(), new ItemDto(0, "item2",
                "item2 of user1", true, null));
        ItemDto item3 = itemService.createItem(user2.getId(), new ItemDto(0, "item3",
                "item3 of user2", true, null));
        bookingService.createBooking(new BookingCreationDto(item3.getId(),
                LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.MILLIS)), user1.getId());
        BookingDto bookingDto2 = bookingService.createBooking(new BookingCreationDto(item1.getId(),
                LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.MILLIS)), user2.getId());
        BookingDto bookingDto3 = bookingService.createBooking(new BookingCreationDto(item2.getId(),
                LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().plusDays(3).truncatedTo(ChronoUnit.MILLIS)), user2.getId());

        assertThatCode(() -> {
            List<BookingDto> requests = bookingService.getBookingsByBookerId(user3.getId(), "ALL", 0, 20);
            assertThat(requests)
                    .as("Проверка получения пустого списка бронирований, когда у пользователя нет бронирований")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            List<BookingDto> requests = bookingService.getBookingsByBookerId(user2.getId(), "ALL", 0, 20);
            assertThat(requests)
                    .as("Проверка получения не пустого списка бронирований, когда у пользователя есть" +
                            " бронирования")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(bookingDto3, Index.atIndex(0))
                    .contains(bookingDto2, Index.atIndex(1));
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getBookingsByBookerId_WhenPASTSearchingState_ThenReturnPastBookings() {
        UserDto user1 = userService.createUser(new UserDto(0, "user1", "user1@mail.ru"));
        UserDto user2 = userService.createUser(new UserDto(0, "user2", "user2@mail.ru"));
        ItemDto item1 = itemService.createItem(user1.getId(), new ItemDto(0, "item1",
                "item1 of user1", true, null));
        ItemDto item2 = itemService.createItem(user1.getId(), new ItemDto(0, "item2",
                "item2 of user1", true, null));
        ItemDto item3 = itemService.createItem(user2.getId(), new ItemDto(0, "item3",
                "item3 of user2", true, null));
        bookingService.createBooking(new BookingCreationDto(item3.getId(),
                LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.MILLIS)), user1.getId());
        BookingDto bookingDto2 = bookingService.createBooking(new BookingCreationDto(item1.getId(),
                LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS)), user2.getId());
        BookingDto bookingDto3 = bookingService.createBooking(new BookingCreationDto(item2.getId(),
                LocalDateTime.now().minusDays(3).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.MILLIS)), user2.getId());

        assertThatCode(() -> {
            List<BookingDto> requests = bookingService.getBookingsByBookerId(user1.getId(), "PAST", 0, 20);
            assertThat(requests)
                    .as("Проверка получения пустого списка бронирований, когда у пользователя нет прошедших" +
                            " бронирований")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            List<BookingDto> requests = bookingService.getBookingsByBookerId(user2.getId(), "PAST", 0, 20);
            assertThat(requests)
                    .as("Проверка получения не пустого списка бронирований, когда у пользователя есть" +
                            "прошедшие бронирования")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(bookingDto2, Index.atIndex(0))
                    .contains(bookingDto3, Index.atIndex(1));
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getBookingsByBookerId_WhenFUTURESearchingState_ThenReturnFutureBookings() {
        UserDto user1 = userService.createUser(new UserDto(0, "user1", "user1@mail.ru"));
        UserDto user2 = userService.createUser(new UserDto(0, "user2", "user2@mail.ru"));
        ItemDto item1 = itemService.createItem(user1.getId(), new ItemDto(0, "item1",
                "item1 of user1", true, null));
        ItemDto item2 = itemService.createItem(user1.getId(), new ItemDto(0, "item2",
                "item2 of user1", true, null));
        ItemDto item3 = itemService.createItem(user2.getId(), new ItemDto(0, "item3",
                "item3 of user2", true, null));
        bookingService.createBooking(new BookingCreationDto(item3.getId(),
                LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS)), user1.getId());
        BookingDto bookingDto2 = bookingService.createBooking(new BookingCreationDto(item1.getId(),
                LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.MILLIS)), user2.getId());
        BookingDto bookingDto3 = bookingService.createBooking(new BookingCreationDto(item2.getId(),
                LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().plusDays(3).truncatedTo(ChronoUnit.MILLIS)), user2.getId());

        assertThatCode(() -> {
            List<BookingDto> requests = bookingService.getBookingsByBookerId(user1.getId(), "FUTURE", 0, 20);
            assertThat(requests)
                    .as("Проверка получения пустого списка бронирований, когда у пользователя нет будущих" +
                            " бронирований")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            List<BookingDto> requests = bookingService.getBookingsByBookerId(user2.getId(), "FUTURE", 0, 20);
            assertThat(requests)
                    .as("Проверка получения не пустого списка бронирований, когда у пользователя есть" +
                            "будущие бронирования")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(bookingDto3, Index.atIndex(0))
                    .contains(bookingDto2, Index.atIndex(1));
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getBookingsByBookerId_WhenCURRENTSearchingState_ThenReturnCurrentBookings() {
        UserDto user1 = userService.createUser(new UserDto(0, "user1", "user1@mail.ru"));
        UserDto user2 = userService.createUser(new UserDto(0, "user2", "user2@mail.ru"));
        ItemDto item1 = itemService.createItem(user1.getId(), new ItemDto(0, "item1",
                "item1 of user1", true, null));
        ItemDto item2 = itemService.createItem(user1.getId(), new ItemDto(0, "item2",
                "item2 of user1", true, null));
        ItemDto item3 = itemService.createItem(user2.getId(), new ItemDto(0, "item3",
                "item3 of user2", true, null));
        bookingService.createBooking(new BookingCreationDto(item3.getId(),
                LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS)), user1.getId());
        BookingDto bookingDto2 = bookingService.createBooking(new BookingCreationDto(item1.getId(),
                LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.MILLIS)), user2.getId());
        BookingDto bookingDto3 = bookingService.createBooking(new BookingCreationDto(item2.getId(),
                LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().plusDays(3).truncatedTo(ChronoUnit.MILLIS)), user2.getId());

        assertThatCode(() -> {
            List<BookingDto> requests = bookingService.getBookingsByBookerId(user1.getId(), "CURRENT", 0, 20);
            assertThat(requests)
                    .as("Проверка получения пустого списка бронирований, когда у пользователя нет текущих" +
                            " бронирований")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            List<BookingDto> requests = bookingService.getBookingsByBookerId(user2.getId(), "CURRENT", 0, 20);
            assertThat(requests)
                    .as("Проверка получения не пустого списка бронирований, когда у пользователя есть" +
                            " текущие бронирования")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(bookingDto2, Index.atIndex(0))
                    .contains(bookingDto3, Index.atIndex(1));
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getBookingsByBookerId_WhenWAITINGSearchingState_ThenReturnWaitingBookings() {
        UserDto user1 = userService.createUser(new UserDto(0, "user1", "user1@mail.ru"));
        UserDto user2 = userService.createUser(new UserDto(0, "user2", "user2@mail.ru"));
        ItemDto item1 = itemService.createItem(user1.getId(), new ItemDto(0, "item1",
                "item1 of user1", true, null));
        ItemDto item2 = itemService.createItem(user1.getId(), new ItemDto(0, "item2",
                "item2 of user1", true, null));
        ItemDto item3 = itemService.createItem(user2.getId(), new ItemDto(0, "item3",
                "item3 of user2", true, null));
        BookingDto bookingDto1 = bookingService.createBooking(new BookingCreationDto(item3.getId(),
                LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS)), user1.getId());
        bookingService.setBookingStatus(user2.getId(), bookingDto1.getId(), true);
        BookingDto bookingDto2 = bookingService.createBooking(new BookingCreationDto(item1.getId(),
                LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.MILLIS)), user2.getId());
        BookingDto bookingDto3 = bookingService.createBooking(new BookingCreationDto(item2.getId(),
                LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().plusDays(3).truncatedTo(ChronoUnit.MILLIS)), user2.getId());

        assertThatCode(() -> {
            List<BookingDto> requests = bookingService.getBookingsByBookerId(user1.getId(), "WAITING", 0, 20);
            assertThat(requests)
                    .as("Проверка получения пустого списка бронирований, когда у пользователя нет" +
                            " бронирований, ожидающих подтверждение")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            List<BookingDto> requests = bookingService.getBookingsByBookerId(user2.getId(), "WAITING", 0, 20);
            assertThat(requests)
                    .as("Проверка получения не пустого списка бронирований, когда у пользователя есть" +
                            " бронирования, ожидающие подтверждение")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(bookingDto2, Index.atIndex(0))
                    .contains(bookingDto3, Index.atIndex(1));
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getBookingsByBookerId_WhenREJECTEDSearchingState_ThenReturnRejectedBookings() {
        UserDto user1 = userService.createUser(new UserDto(0, "user1", "user1@mail.ru"));
        UserDto user2 = userService.createUser(new UserDto(0, "user2", "user2@mail.ru"));
        ItemDto item1 = itemService.createItem(user1.getId(), new ItemDto(0, "item1",
                "item1 of user1", true, null));
        ItemDto item2 = itemService.createItem(user1.getId(), new ItemDto(0, "item2",
                "item2 of user1", true, null));
        ItemDto item3 = itemService.createItem(user2.getId(), new ItemDto(0, "item3",
                "item3 of user2", true, null));
        BookingDto bookingDto1 = bookingService.createBooking(new BookingCreationDto(item3.getId(),
                LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS)), user1.getId());
        bookingService.setBookingStatus(user2.getId(), bookingDto1.getId(), true);
        BookingDto bookingDto2 = bookingService.createBooking(new BookingCreationDto(item1.getId(),
                LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.MILLIS)), user2.getId());
        BookingDto bookingDto3 = bookingService.createBooking(new BookingCreationDto(item2.getId(),
                LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().plusDays(3).truncatedTo(ChronoUnit.MILLIS)), user2.getId());
        BookingDto expectedBookingDto1 = bookingService.setBookingStatus(user1.getId(), bookingDto2.getId(), false);
        BookingDto expectedBookingDto2 = bookingService.setBookingStatus(user1.getId(), bookingDto3.getId(), false);

        assertThatCode(() -> {
            List<BookingDto> requests = bookingService.getBookingsByBookerId(user1.getId(), "REJECTED", 0, 20);
            assertThat(requests)
                    .as("Проверка получения пустого списка бронирований, когда у пользователя нет" +
                            " отклоненных бронирований")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            List<BookingDto> requests = bookingService.getBookingsByBookerId(user2.getId(), "REJECTED", 0, 20);
            assertThat(requests)
                    .as("Проверка получения не пустого списка бронирований, когда у пользователя есть" +
                            " отклоненные бронирования")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(expectedBookingDto1, Index.atIndex(0))
                    .contains(expectedBookingDto2, Index.atIndex(1));
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getBookingsByOwnerId_WhenALLSearchingState_ThenReturnAllBookings() {
        UserDto user1 = userService.createUser(new UserDto(0, "user1", "user1@mail.ru"));
        UserDto user2 = userService.createUser(new UserDto(0, "user2", "user2@mail.ru"));
        UserDto user3 = userService.createUser(new UserDto(0, "user3", "user3@mail.ru"));
        ItemDto item1 = itemService.createItem(user1.getId(), new ItemDto(0, "item1",
                "item1 of user1", true, null));
        ItemDto item2 = itemService.createItem(user1.getId(), new ItemDto(0, "item2",
                "item2 of user1", true, null));
        ItemDto item3 = itemService.createItem(user2.getId(), new ItemDto(0, "item3",
                "item3 of user2", true, null));
        bookingService.createBooking(new BookingCreationDto(item3.getId(),
                LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.MILLIS)), user1.getId());
        BookingDto bookingDto2 = bookingService.createBooking(new BookingCreationDto(item1.getId(),
                LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.MILLIS)), user2.getId());
        BookingDto bookingDto3 = bookingService.createBooking(new BookingCreationDto(item2.getId(),
                LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().plusDays(3).truncatedTo(ChronoUnit.MILLIS)), user2.getId());

        assertThatCode(() -> {
            List<BookingDto> requests = bookingService.getBookingsByOwnerId(user3.getId(), "ALL", 0, 20);
            assertThat(requests)
                    .as("Проверка получения пустого списка бронирований, когда у пользователя нет бронируемых" +
                            " вещей")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            List<BookingDto> requests = bookingService.getBookingsByOwnerId(user1.getId(), "ALL", 0, 20);
            assertThat(requests)
                    .as("Проверка получения не пустого списка бронирований, когда у пользователя есть" +
                            " бронирования вещей")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(bookingDto3, Index.atIndex(0))
                    .contains(bookingDto2, Index.atIndex(1));
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getBookingsByOwnerId_WhenPASTSearchingState_ThenReturnPastBookings() {
        UserDto user1 = userService.createUser(new UserDto(0, "user1", "user1@mail.ru"));
        UserDto user2 = userService.createUser(new UserDto(0, "user2", "user2@mail.ru"));
        ItemDto item1 = itemService.createItem(user1.getId(), new ItemDto(0, "item1",
                "item1 of user1", true, null));
        ItemDto item2 = itemService.createItem(user1.getId(), new ItemDto(0, "item2",
                "item2 of user1", true, null));
        ItemDto item3 = itemService.createItem(user2.getId(), new ItemDto(0, "item3",
                "item3 of user2", true, null));
        bookingService.createBooking(new BookingCreationDto(item3.getId(),
                LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.MILLIS)), user1.getId());
        BookingDto bookingDto2 = bookingService.createBooking(new BookingCreationDto(item1.getId(),
                LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS)), user2.getId());
        BookingDto bookingDto3 = bookingService.createBooking(new BookingCreationDto(item2.getId(),
                LocalDateTime.now().minusDays(3).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.MILLIS)), user2.getId());

        assertThatCode(() -> {
            List<BookingDto> requests = bookingService.getBookingsByOwnerId(user2.getId(), "PAST", 0, 20);
            assertThat(requests)
                    .as("Проверка получения пустого списка бронирований, когда у пользователя нет прошедших" +
                            " бронирований вещей")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            List<BookingDto> requests = bookingService.getBookingsByOwnerId(user1.getId(), "PAST", 0, 20);
            assertThat(requests)
                    .as("Проверка получения не пустого списка бронирований, когда у пользователя есть" +
                            "прошедшие бронирования вещей")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(bookingDto2, Index.atIndex(0))
                    .contains(bookingDto3, Index.atIndex(1));
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getBookingsByOwnerId_WhenFUTURESearchingState_ThenReturnFutureBookings() {
        UserDto user1 = userService.createUser(new UserDto(0, "user1", "user1@mail.ru"));
        UserDto user2 = userService.createUser(new UserDto(0, "user2", "user2@mail.ru"));
        ItemDto item1 = itemService.createItem(user1.getId(), new ItemDto(0, "item1",
                "item1 of user1", true, null));
        ItemDto item2 = itemService.createItem(user1.getId(), new ItemDto(0, "item2",
                "item2 of user1", true, null));
        ItemDto item3 = itemService.createItem(user2.getId(), new ItemDto(0, "item3",
                "item3 of user2", true, null));
        bookingService.createBooking(new BookingCreationDto(item3.getId(),
                LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS)), user1.getId());
        BookingDto bookingDto2 = bookingService.createBooking(new BookingCreationDto(item1.getId(),
                LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.MILLIS)), user2.getId());
        BookingDto bookingDto3 = bookingService.createBooking(new BookingCreationDto(item2.getId(),
                LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().plusDays(3).truncatedTo(ChronoUnit.MILLIS)), user2.getId());

        assertThatCode(() -> {
            List<BookingDto> requests = bookingService.getBookingsByOwnerId(user2.getId(), "FUTURE", 0, 20);
            assertThat(requests)
                    .as("Проверка получения пустого списка бронирований, когда у пользователя нет будущих" +
                            " бронирований вещей")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            List<BookingDto> requests = bookingService.getBookingsByOwnerId(user1.getId(), "FUTURE", 0, 20);
            assertThat(requests)
                    .as("Проверка получения не пустого списка бронирований, когда у пользователя есть" +
                            "будущие бронирования вещей")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(bookingDto3, Index.atIndex(0))
                    .contains(bookingDto2, Index.atIndex(1));
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getBookingsByOwnerId_WhenCURRENTSearchingState_ThenReturnCurrentBookings() {
        UserDto user1 = userService.createUser(new UserDto(0, "user1", "user1@mail.ru"));
        UserDto user2 = userService.createUser(new UserDto(0, "user2", "user2@mail.ru"));
        ItemDto item1 = itemService.createItem(user1.getId(), new ItemDto(0, "item1",
                "item1 of user1", true, null));
        ItemDto item2 = itemService.createItem(user1.getId(), new ItemDto(0, "item2",
                "item2 of user1", true, null));
        ItemDto item3 = itemService.createItem(user2.getId(), new ItemDto(0, "item3",
                "item3 of user2", true, null));
        bookingService.createBooking(new BookingCreationDto(item3.getId(),
                LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS)), user1.getId());
        BookingDto bookingDto2 = bookingService.createBooking(new BookingCreationDto(item1.getId(),
                LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.MILLIS)), user2.getId());
        BookingDto bookingDto3 = bookingService.createBooking(new BookingCreationDto(item2.getId(),
                LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().plusDays(3).truncatedTo(ChronoUnit.MILLIS)), user2.getId());

        assertThatCode(() -> {
            List<BookingDto> requests = bookingService.getBookingsByOwnerId(user2.getId(), "CURRENT", 0, 20);
            assertThat(requests)
                    .as("Проверка получения пустого списка бронирований, когда у пользователя нет текущих" +
                            " бронирований вещей")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            List<BookingDto> requests = bookingService.getBookingsByOwnerId(user1.getId(), "CURRENT", 0, 20);
            assertThat(requests)
                    .as("Проверка получения не пустого списка бронирований, когда у пользователя есть" +
                            " текущие бронирования вещей")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(bookingDto2, Index.atIndex(0))
                    .contains(bookingDto3, Index.atIndex(1));
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getBookingsByOwnerId_WhenWAITINGSearchingState_ThenReturnWaitingBookings() {
        UserDto user1 = userService.createUser(new UserDto(0, "user1", "user1@mail.ru"));
        UserDto user2 = userService.createUser(new UserDto(0, "user2", "user2@mail.ru"));
        ItemDto item1 = itemService.createItem(user1.getId(), new ItemDto(0, "item1",
                "item1 of user1", true, null));
        ItemDto item2 = itemService.createItem(user1.getId(), new ItemDto(0, "item2",
                "item2 of user1", true, null));
        ItemDto item3 = itemService.createItem(user2.getId(), new ItemDto(0, "item3",
                "item3 of user2", true, null));
        BookingDto bookingDto1 = bookingService.createBooking(new BookingCreationDto(item3.getId(),
                LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS)), user1.getId());
        bookingService.setBookingStatus(user2.getId(), bookingDto1.getId(), true);
        BookingDto bookingDto2 = bookingService.createBooking(new BookingCreationDto(item1.getId(),
                LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.MILLIS)), user2.getId());
        BookingDto bookingDto3 = bookingService.createBooking(new BookingCreationDto(item2.getId(),
                LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().plusDays(3).truncatedTo(ChronoUnit.MILLIS)), user2.getId());

        assertThatCode(() -> {
            List<BookingDto> requests = bookingService.getBookingsByOwnerId(user2.getId(), "WAITING", 0, 20);
            assertThat(requests)
                    .as("Проверка получения пустого списка бронирований, когда у пользователя нет" +
                            " бронирований, ожидающих подтверждение")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            List<BookingDto> requests = bookingService.getBookingsByOwnerId(user1.getId(), "WAITING", 0, 20);
            assertThat(requests)
                    .as("Проверка получения не пустого списка бронирований, когда у пользователя есть" +
                            " бронирования, ожидающие подтверждение")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(bookingDto2, Index.atIndex(0))
                    .contains(bookingDto3, Index.atIndex(1));
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getBookingsByOwnerId_WhenREJECTEDSearchingState_ThenReturnRejectedBookings() {
        UserDto user1 = userService.createUser(new UserDto(0, "user1", "user1@mail.ru"));
        UserDto user2 = userService.createUser(new UserDto(0, "user2", "user2@mail.ru"));
        ItemDto item1 = itemService.createItem(user1.getId(), new ItemDto(0, "item1",
                "item1 of user1", true, null));
        ItemDto item2 = itemService.createItem(user1.getId(), new ItemDto(0, "item2",
                "item2 of user1", true, null));
        ItemDto item3 = itemService.createItem(user2.getId(), new ItemDto(0, "item3",
                "item3 of user2", true, null));
        BookingDto bookingDto1 = bookingService.createBooking(new BookingCreationDto(item3.getId(),
                LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS)), user1.getId());
        bookingService.setBookingStatus(user2.getId(), bookingDto1.getId(), true);
        BookingDto bookingDto2 = bookingService.createBooking(new BookingCreationDto(item1.getId(),
                LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.MILLIS)), user2.getId());
        BookingDto bookingDto3 = bookingService.createBooking(new BookingCreationDto(item2.getId(),
                LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.MILLIS),
                LocalDateTime.now().plusDays(3)), user2.getId());
        BookingDto expectedBookingDto1 = bookingService.setBookingStatus(user1.getId(), bookingDto2.getId(),
                false);
        BookingDto expectedBookingDto2 = bookingService.setBookingStatus(user1.getId(), bookingDto3.getId(),
                false);

        assertThatCode(() -> {
            List<BookingDto> requests = bookingService.getBookingsByOwnerId(user2.getId(), "REJECTED", 0, 20);
            assertThat(requests)
                    .as("Проверка получения пустого списка бронирований, когда у пользователя нет" +
                            " отклоненных бронирований")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            List<BookingDto> requests = bookingService.getBookingsByOwnerId(user1.getId(), "REJECTED", 0, 20);
            assertThat(requests)
                    .as("Проверка получения не пустого списка бронирований, когда у пользователя есть" +
                            " отклоненные бронирования")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(expectedBookingDto1, Index.atIndex(0))
                    .contains(expectedBookingDto2, Index.atIndex(1));
        }).doesNotThrowAnyException();
    }
}