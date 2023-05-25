package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotAvailableException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemStorage;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserStorage;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.utils.Constants;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @Mock
    private BookingStorage bookingStorage;
    @Mock
    private ItemStorage itemStorage;
    @Mock
    private UserStorage userStorage;
    @InjectMocks
    private BookingServiceImpl bookingService;
    @Captor
    private ArgumentCaptor<Booking> argumentCaptor;

    @Test
    void createBooking_WhenBookingTimeIsIncorrect_ThenThrowsIllegalArgumentException() {
        long userId = 1;
        BookingCreationDto endBeforeStartDto = new BookingCreationDto(1L, LocalDateTime.now(),
                LocalDateTime.now().minusDays(1));
        LocalDateTime time = LocalDateTime.now();
        BookingCreationDto endEqualsStartDto = new BookingCreationDto(1L, time, time);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .as("Проверка добавления бронирования с временем конца бронирования раньше времени начала")
                .isThrownBy(() -> bookingService.createBooking(endBeforeStartDto, userId))
                .withMessage(Constants.WRONG_START_AND_END_BOOKING_DATES_MESSAGE);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .as("Проверка добавления бронирования с временем конца бронирования равным времени начала")
                .isThrownBy(() -> bookingService.createBooking(endEqualsStartDto, userId))
                .withMessage(Constants.WRONG_START_AND_END_BOOKING_DATES_MESSAGE);

        verifyNoInteractions(bookingStorage);
    }

    @Test
    void createBooking_WhenUserDoesNotExist_ThenThrowsNotFoundException() {
        long userId = 1;
        BookingCreationDto bookingCreationDto = new BookingCreationDto(1L, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3));
        when(userStorage.findById(userId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .as("Проверка добавления бронирования пользователем, id которого не найдено в базе")
                .isThrownBy(() -> bookingService.createBooking(bookingCreationDto, userId))
                .withMessage(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));

        verify(userStorage, Mockito.times(1)).findById(userId);
        verifyNoInteractions(bookingStorage);
    }

    @Test
    void createBooking_WhenItemDoesNotExist_ThenThrowsNotFoundException() {
        long userId = 1;
        BookingCreationDto bookingCreationDto = new BookingCreationDto(1L, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3));
        User user = new User(userId, "name", "mail@mail.ru");
        when(userStorage.findById(userId)).thenReturn(Optional.of(user));
        when(itemStorage.findById(bookingCreationDto.getItemId())).thenReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .as("Проверка добавления бронирования на вещь, id которой нет в базе")
                .isThrownBy(() -> bookingService.createBooking(bookingCreationDto, userId))
                .withMessage(String.format(Constants.ITEM_NOT_FOUND_MESSAGE, bookingCreationDto.getItemId()));

        verify(itemStorage, Mockito.times(1)).findById(bookingCreationDto.getItemId());
        verifyNoInteractions(bookingStorage);
    }

    @Test
    void createBooking_WhenItemDoesNotAvailable_ThenThrowsNotAvailableException() {
        long userId = 1;
        BookingCreationDto bookingCreationDto = new BookingCreationDto(1L, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3));
        User user = new User(userId, "name", "mail@mail.ru");
        Item item = new Item(bookingCreationDto.getItemId(), "name", "description", false,
                new User(userId + 1), null);
        when(userStorage.findById(userId)).thenReturn(Optional.of(user));
        when(itemStorage.findById(bookingCreationDto.getItemId())).thenReturn(Optional.of(item));

        assertThatExceptionOfType(NotAvailableException.class)
                .as("Проверка добавления бронирования на вещь, которая не доступна к бронированию")
                .isThrownBy(() -> bookingService.createBooking(bookingCreationDto, userId))
                .withMessage(String.format(Constants.ITEM_NOT_AVAILABLE_MESSAGE, bookingCreationDto.getItemId()));

        verifyNoInteractions(bookingStorage);
    }

    @Test
    void createBooking_WhenBookerIsItemOwner_ThenThrowsSecurityException() {
        long userId = 1;
        BookingCreationDto bookingCreationDto = new BookingCreationDto(1L, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3));
        User user = new User(userId, "name", "mail@mail.ru");
        Item item = new Item(bookingCreationDto.getItemId(), "name", "description", true,
                new User(userId), null);
        when(userStorage.findById(userId)).thenReturn(Optional.of(user));
        when(itemStorage.findById(bookingCreationDto.getItemId())).thenReturn(Optional.of(item));

        assertThatExceptionOfType(SecurityException.class)
                .as("Проверка добавления бронирования на вещь, владельцем этой вещи")
                .isThrownBy(() -> bookingService.createBooking(bookingCreationDto, userId))
                .withMessage(Constants.USER_CANNOT_BOOK_HIS_ITEM_MESSAGE);

        verifyNoInteractions(bookingStorage);
    }

    @Test
    void createBooking_WhenBookingTimeHasCrossings_ThenThrowsAlreadyExistException() {
        long userId = 1;
        BookingCreationDto bookingCreationDto = new BookingCreationDto(1L, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3));
        User user = new User(userId, "name", "mail@mail.ru");
        Item item = new Item(bookingCreationDto.getItemId(), "name", "description", true,
                new User(userId + 1), null);
        Booking booking = new Booking(1L, Status.APPROVED, item, new User(userId + 1),
                LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(7));
        when(userStorage.findById(userId)).thenReturn(Optional.of(user));
        when(itemStorage.findById(bookingCreationDto.getItemId())).thenReturn(Optional.of(item));
        when(bookingStorage.findByItem_IdAndEndAfterAndStatusOrderByStartAsc(anyLong(), any(LocalDateTime.class),
                any(Status.class))).thenReturn(List.of(booking));

        assertThatExceptionOfType(AlreadyExistException.class)
                .as("Проверка добавления бронирования на вещь, когда время этого бронирования пересекается " +
                        "с временем других бронирований")
                .isThrownBy(() -> bookingService.createBooking(bookingCreationDto, userId))
                .withMessage(Constants.TIME_NOT_AVAILABLE_FOR_BOOKING_MESSAGE,
                        bookingCreationDto.getStart().format(BookingServiceImpl.FORMATTER),
                        bookingCreationDto.getEnd().format(BookingServiceImpl.FORMATTER));

        verify(bookingStorage, Mockito.times(1)).findByItem_IdAndEndAfterAndStatusOrderByStartAsc(
                anyLong(), any(LocalDateTime.class), any(Status.class));
        verifyNoMoreInteractions(bookingStorage);
    }

    @Test
    void createBooking_WhenAllParametersCorrect_ThenCreateBooking() {
        long userId = 1;
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(3);
        BookingCreationDto bookingCreationDto = new BookingCreationDto(1L, start, end);
        User user = new User(userId, "name", "mail@mail.ru");
        Item item = new Item(bookingCreationDto.getItemId(), "name", "description", true,
                new User(userId + 1), null);
        Booking existedBooking = new Booking(1L, Status.APPROVED, item, new User(userId + 1),
                LocalDateTime.now().plusHours(4), LocalDateTime.now().plusHours(7));
        Booking createdBooking = new Booking(2L, Status.WAITING, item, user, start, end);
        when(userStorage.findById(userId)).thenReturn(Optional.of(user));
        when(itemStorage.findById(bookingCreationDto.getItemId())).thenReturn(Optional.of(item));
        when(bookingStorage.findByItem_IdAndEndAfterAndStatusOrderByStartAsc(anyLong(), any(LocalDateTime.class),
                any(Status.class))).thenReturn(List.of(existedBooking));
        when(bookingStorage.save(any(Booking.class))).thenReturn(createdBooking);
        BookingDto expectedDto = new BookingDto(2L, start, end, Status.WAITING, new UserDto(userId,
                "name", "mail@mail.ru"), new ItemDto(bookingCreationDto.getItemId(), "name", "description",
                true, null));

        assertThatCode(() -> {
            BookingDto actualDto = bookingService.createBooking(bookingCreationDto, userId);

            assertThat(actualDto)
                    .as("Проверка создания бронирования при корректных входных данных")
                    .isNotNull()
                    .isEqualTo(expectedDto);
        }).doesNotThrowAnyException();

        verify(bookingStorage, Mockito.times(1)).save(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue())
                .as("Проверка передаваемого аргумента в метод сохранения бронирования")
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", null)
                .hasFieldOrPropertyWithValue("status", Status.WAITING)
                .hasFieldOrPropertyWithValue("item", item)
                .hasFieldOrPropertyWithValue("booker", user)
                .hasFieldOrPropertyWithValue("start", start)
                .hasFieldOrPropertyWithValue("end", end);
        verifyNoMoreInteractions(bookingStorage);
    }

    @Test
    void setBookingStatus_WhenBookingDoesNotExist_ThenThrowsNotFoundException() {
        long userId = 1;
        long bookingId = 1;
        boolean approved = true;
        when(bookingStorage.findById(bookingId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .as("Проверка изменения статуса бронирования id которого нет в базе")
                .isThrownBy(() -> bookingService.setBookingStatus(userId, bookingId, approved))
                .withMessage(String.format(Constants.BOOKING_NOT_FOUND_MESSAGE, bookingId));

        verify(bookingStorage, Mockito.times(1)).findById(bookingId);
        verifyNoMoreInteractions(bookingStorage);
    }

    @Test
    void setBookingStatus_WhenUserDoesNotItemOwner_ThenThrowsSecurityException() {
        long userId = 1;
        long bookingId = 1;
        boolean approved = true;
        User user = new User(userId, "name", "mail@mail.ru");
        Item item = new Item(1L, "name", "description", true, new User(userId + 1),
                null);
        Booking booking = new Booking(1L, Status.WAITING, item, user, LocalDateTime.now().plusHours(4),
                LocalDateTime.now().plusHours(7));
        when(bookingStorage.findById(bookingId)).thenReturn(Optional.of(booking));
        when(itemStorage.existsByUser_IdAndId(userId, item.getId())).thenReturn(false);

        assertThatExceptionOfType(SecurityException.class)
                .as("Проверка изменения статуса бронирования пользователем, который не является владельцем вещи")
                .isThrownBy(() -> bookingService.setBookingStatus(userId, bookingId, approved))
                .withMessage(Constants.NOT_OWNER_CANNOT_CHANGE_BOOKING_STATUS_MESSAGE);

        verify(bookingStorage, Mockito.times(1)).findById(bookingId);
        verify(itemStorage, Mockito.times(1)).existsByUser_IdAndId(userId, item.getId());
        verifyNoMoreInteractions(bookingStorage);
    }

    @Test
    void setBookingStatus_WhenStatusHasAlreadyBeenChanged_ThenThrowsIllegalArgumentException() {
        long userId = 1;
        long bookingId = 1;
        boolean approved = true;
        User user = new User(userId, "name", "mail@mail.ru");
        Item item = new Item(1L, "name", "description", true, new User(userId),
                null);
        Booking booking = new Booking(1L, Status.APPROVED, item, user, LocalDateTime.now().plusHours(4),
                LocalDateTime.now().plusHours(7));
        when(bookingStorage.findById(bookingId)).thenReturn(Optional.of(booking));
        when(itemStorage.existsByUser_IdAndId(userId, item.getId())).thenReturn(true);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .as("Проверка изменения статуса бронирования, когда статус бронирования уже был изменен")
                .isThrownBy(() -> bookingService.setBookingStatus(userId, bookingId, approved))
                .withMessage(Constants.USER_CANNOT_CHANGE_BOOKING_STATUS_TWICE_MESSAGE);

        verify(bookingStorage, Mockito.times(1)).findById(bookingId);
        verify(itemStorage, Mockito.times(1)).existsByUser_IdAndId(userId, item.getId());
        verifyNoMoreInteractions(bookingStorage);
    }

    @Test
    void setBookingStatus_WhenUserApprovesBooking_ThenSetBookingStatusApproved() {
        long userId = 1;
        long bookingId = 1;
        boolean approved = true;
        User user = new User(userId, "name", "mail@mail.ru");
        Item item = new Item(1L, "name", "description", true, new User(userId),
                null);
        LocalDateTime start = LocalDateTime.now().plusHours(4).truncatedTo(ChronoUnit.MILLIS);
        LocalDateTime end = LocalDateTime.now().plusHours(7).truncatedTo(ChronoUnit.MILLIS);
        Booking booking = new Booking(1L, Status.WAITING, item, user, start, end);
        Booking updatedBooking = new Booking(1L, Status.APPROVED, item, user, start, end);
        BookingDto expectedDto = new BookingDto(1L, start, end, Status.APPROVED,
                new UserDto(userId, "name", "mail@mail.ru"), new ItemDto(1L, "name",
                "description", true, null));
        when(bookingStorage.findById(bookingId)).thenReturn(Optional.of(booking));
        when(itemStorage.existsByUser_IdAndId(userId, item.getId())).thenReturn(true);
        when(bookingStorage.save(any(Booking.class))).thenReturn(updatedBooking);

        assertThatCode(() -> {
            BookingDto bookingDto = bookingService.setBookingStatus(userId, bookingId, approved);

            assertThat(bookingDto)
                    .as("Проверка установки подтвержденного статуса бронирования")
                    .isNotNull()
                    .isEqualTo(expectedDto);
        }).doesNotThrowAnyException();

        verify(bookingStorage, Mockito.times(1)).findById(bookingId);
        verify(itemStorage, Mockito.times(1)).existsByUser_IdAndId(userId, item.getId());
        verify(bookingStorage, Mockito.times(1)).save(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue())
                .as("Проверка передаваемого аргумента в метод изменения статуса бронирования")
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", updatedBooking.getId())
                .hasFieldOrPropertyWithValue("status", updatedBooking.getStatus())
                .hasFieldOrPropertyWithValue("item", updatedBooking.getItem())
                .hasFieldOrPropertyWithValue("booker", updatedBooking.getBooker())
                .hasFieldOrPropertyWithValue("start", updatedBooking.getStart())
                .hasFieldOrPropertyWithValue("end", updatedBooking.getEnd());
    }

    @Test
    void setBookingStatus_WhenUserDoesNotApproveBooking_ThenSetBookingStatusRejected() {
        long userId = 1;
        long bookingId = 1;
        boolean approved = false;
        User user = new User(userId, "name", "mail@mail.ru");
        Item item = new Item(1L, "name", "description", true, new User(userId),
                null);
        LocalDateTime start = LocalDateTime.now().plusHours(4).truncatedTo(ChronoUnit.MILLIS);
        LocalDateTime end = LocalDateTime.now().plusHours(7).truncatedTo(ChronoUnit.MILLIS);
        Booking booking = new Booking(1L, Status.WAITING, item, user, start, end);
        Booking updatedBooking = new Booking(1L, Status.REJECTED, item, user, start, end);
        BookingDto expectedDto = new BookingDto(1L, start, end, Status.REJECTED,
                new UserDto(userId, "name", "mail@mail.ru"), new ItemDto(1L, "name",
                "description", true, null));
        when(bookingStorage.findById(bookingId)).thenReturn(Optional.of(booking));
        when(itemStorage.existsByUser_IdAndId(userId, item.getId())).thenReturn(true);
        when(bookingStorage.save(any(Booking.class))).thenReturn(updatedBooking);

        assertThatCode(() -> {
            BookingDto bookingDto = bookingService.setBookingStatus(userId, bookingId, approved);

            assertThat(bookingDto)
                    .as("Проверка установки отклоненного статуса бронирования")
                    .isNotNull()
                    .isEqualTo(expectedDto);
        }).doesNotThrowAnyException();

        verify(bookingStorage, Mockito.times(1)).findById(bookingId);
        verify(itemStorage, Mockito.times(1)).existsByUser_IdAndId(userId, item.getId());
        verify(bookingStorage, Mockito.times(1)).save(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue())
                .as("Проверка передаваемого аргумента в метод изменения статуса бронирования")
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", updatedBooking.getId())
                .hasFieldOrPropertyWithValue("status", updatedBooking.getStatus())
                .hasFieldOrPropertyWithValue("item", updatedBooking.getItem())
                .hasFieldOrPropertyWithValue("booker", updatedBooking.getBooker())
                .hasFieldOrPropertyWithValue("start", updatedBooking.getStart())
                .hasFieldOrPropertyWithValue("end", updatedBooking.getEnd());
    }

    @Test
    void getBooking_WhenBookingDoesNotExist_ThenThrowsNotFoundException() {
        long userId = 1;
        long bookingId = 1;
        when(bookingStorage.findById(bookingId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .as("Проверка получения бронирования по id, которого нет в базе")
                .isThrownBy(() -> bookingService.getBooking(userId, bookingId))
                .withMessage(String.format(Constants.BOOKING_NOT_FOUND_MESSAGE, bookingId));

        verify(bookingStorage, Mockito.times(1)).findById(bookingId);
        verifyNoMoreInteractions(bookingStorage);
    }

    @Test
    void getBooking_WhenUserDoesNotItemOrBookingOwner_ThenThrowsSecurityException() {
        long userId = 1;
        long bookingId = 1;
        User user = new User(userId + 1, "name", "mail@mail.ru");
        Item item = new Item(1L, "name", "description", true, new User(userId + 2),
                null);
        Booking booking = new Booking(1L, Status.WAITING, item, user, LocalDateTime.now().plusHours(4),
                LocalDateTime.now().plusHours(7));
        when(bookingStorage.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatExceptionOfType(SecurityException.class)
                .as("Проверка получения бронирования пользователем, который не является владельцем " +
                        "бронирования или вещи")
                .isThrownBy(() -> bookingService.getBooking(userId, bookingId))
                .withMessage(String.format(Constants.NOT_BOOKING_OR_ITEM_OWNER_CANNOT_GET_BOOKING_MESSAGE));

        verify(bookingStorage, Mockito.times(1)).findById(bookingId);
        verifyNoMoreInteractions(bookingStorage);
    }

    @Test
    void getBooking_WhenUserIsBookingOwner_ThenReturnBooking() {
        long userId = 1;
        long bookingId = 1;
        User user = new User(userId, "name", "mail@mail.ru");
        Item item = new Item(1L, "name", "description", true, new User(userId + 2),
                null);
        Booking booking = new Booking(1L, Status.WAITING, item, user, LocalDateTime.now().plusHours(4),
                LocalDateTime.now().plusHours(7));
        BookingDto expectedDto = new BookingDto(1L, booking.getStart(), booking.getEnd(), Status.WAITING,
                new UserDto(userId, "name", "mail@mail.ru"), new ItemDto(1L, "name",
                "description", true, null));
        when(bookingStorage.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatCode(() -> {
            BookingDto bookingDto = bookingService.getBooking(userId, bookingId);

            assertThat(bookingDto)
                    .as("Проверка получения бронирования по id, когда пользователь является владельцем" +
                            " бронирования")
                    .isNotNull()
                    .isEqualTo(expectedDto);
        }).doesNotThrowAnyException();

        verify(bookingStorage, Mockito.times(1)).findById(bookingId);
        verifyNoMoreInteractions(bookingStorage);
    }

    @Test
    void getBooking_WhenUserIsItemOwner_ThenReturnBooking() {
        long userId = 1;
        long bookingId = 1;
        User user = new User(userId + 1, "name", "mail@mail.ru");
        Item item = new Item(1L, "name", "description", true, new User(userId),
                null);
        Booking booking = new Booking(1L, Status.WAITING, item, user, LocalDateTime.now().plusHours(4),
                LocalDateTime.now().plusHours(7));
        BookingDto expectedDto = new BookingDto(1L, booking.getStart(), booking.getEnd(), Status.WAITING,
                new UserDto(userId + 1, "name", "mail@mail.ru"), new ItemDto(1L, "name",
                "description", true, null));
        when(bookingStorage.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatCode(() -> {
            BookingDto bookingDto = bookingService.getBooking(userId, bookingId);

            assertThat(bookingDto)
                    .as("Проверка получения бронирования по id, когда пользователь является владельцем" +
                            " вещи")
                    .isNotNull()
                    .isEqualTo(expectedDto);
        }).doesNotThrowAnyException();

        verify(bookingStorage, Mockito.times(1)).findById(bookingId);
        verifyNoMoreInteractions(bookingStorage);
    }

    @Test
    void getBookingsByBookerId_WhenStateIsIncorrect_ThenThrowsIllegalArgumentException() {
        long userId = 1;
        String state = "wrong_state";

        assertThatExceptionOfType(IllegalArgumentException.class)
                .as("Проверка получения бронирований по id владельца бронирования, когда такого состояния" +
                        " не существует")
                .isThrownBy(() -> bookingService.getBookingsByBookerId(userId, state, 0, 20))
                .withMessage(String.format(Constants.UNKNOWN_SEARCHING_STATE_MESSAGE, state));
    }

    @Test
    void getBookingsByBookerId_WhenUserDoesNotExist_ThenThrowsNotFoundException() {
        long userId = 1;
        String state = "ALL";
        when(userStorage.existsById(userId)).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class)
                .as("Проверка получения бронирований по id владельца бронирования, когда пользователь с таким" +
                        " id не найден в базе")
                .isThrownBy(() -> bookingService.getBookingsByBookerId(userId, state, 0, 20))
                .withMessage(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
    }

    @Test
    void getBookingsByOwnerId_WhenStateIsIncorrect_ThenThrowsIllegalArgumentException() {
        long userId = 1;
        String state = "wrong_state";

        assertThatExceptionOfType(IllegalArgumentException.class)
                .as("Проверка получения бронирований по id владельца вещи, когда такого состояния" +
                        " не существует")
                .isThrownBy(() -> bookingService.getBookingsByOwnerId(userId, state, 0, 20))
                .withMessage(String.format(Constants.UNKNOWN_SEARCHING_STATE_MESSAGE, state));
    }

    @Test
    void getBookingsByOwnerId_WhenUserDoesNotExist_ThenThrowsNotFoundException() {
        long userId = 1;
        String state = "ALL";
        when(userStorage.existsById(userId)).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class)
                .as("Проверка получения бронирований по id владельца бронирования, когда пользователь с таким" +
                        " id не найден в базе")
                .isThrownBy(() -> bookingService.getBookingsByOwnerId(userId, state, 0, 20))
                .withMessage(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
    }
}