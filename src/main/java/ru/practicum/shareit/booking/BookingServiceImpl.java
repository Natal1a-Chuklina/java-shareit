package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.SearchingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingMapper;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.NotAvailableException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemStorage;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.utils.Constants;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static ru.practicum.shareit.utils.Constants.USER_CANNOT_CHANGE_BOOKING_STATUS_TWICE_MESSAGE;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingStorage bookingStorage;
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public BookingDto createBooking(BookingCreationDto bookingDto, long userId) {
        checkTimeCorrectness(bookingDto.getStart(), bookingDto.getEnd());

        Optional<User> user = userStorage.findById(userId);
        if (user.isEmpty()) {
            log.warn("Выполнена попытка забронировать вещь пользователем с несуществующим id = {}", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        }

        Optional<Item> item = itemStorage.findById(bookingDto.getItemId());
        if (item.isEmpty()) {
            log.warn("Выполнена попытка забронировать вещь с несуществующим id = {} пользователем с id = {}",
                    bookingDto.getItemId(), userId);
            throw new NotFoundException(String.format(Constants.ITEM_NOT_FOUND_MESSAGE, bookingDto.getItemId()));
        }

        if (Boolean.FALSE.equals(item.get().getAvailable())) {
            log.warn("Выполнена попытка пользователем с id = {} забронировать вещь с id = {}, которая" +
                    " недоступна к бронированию", userId, bookingDto.getItemId());
            throw new NotAvailableException(String.format(Constants.ITEM_NOT_AVAILABLE_MESSAGE,
                    bookingDto.getItemId()));
        }

        if (item.get().getUser().getId().equals(userId)) {
            log.warn("Выполнена попытка пользователем с id = {} забронировать свою вещь с id = {}",
                    userId, item.get().getId());
            throw new SecurityException(Constants.USER_CANNOT_BOOK_HIS_ITEM_MESSAGE);
        }

        Booking createdBooking = bookingStorage.save(BookingMapper.toBooking(bookingDto, user.get(), item.get()));
        log.info("Создано бронирование с id = {} пользователем с id = {} на вещь с id = {}", createdBooking.getId(),
                userId, bookingDto.getItemId());
        return BookingMapper.toBookingDto(createdBooking);
    }

    @Override
    public BookingDto setBookingStatus(long userId, long bookingId, boolean approved) {
        Booking booking = getBookingById(bookingId);

        if (!itemStorage.existsByUser_IdAndId(userId, booking.getItem().getId())) {
            log.warn("Выполнена попытка изменить статус бронирования вещи с id = {} пользователем с id = {}, который " +
                    "не является ее владельцем", booking.getItem().getId(), userId);
            throw new SecurityException(Constants.NOT_OWNER_CANNOT_CHANGE_BOOKING_STATUS_MESSAGE);
        }

        if (!booking.getStatus().equals(Status.WAITING)) {
            log.warn("Выполнена попытка повторно изменить статус бронирования вещи с id = {} пользователем с id = {}",
                    bookingId, userId);
            throw new IllegalArgumentException(USER_CANNOT_CHANGE_BOOKING_STATUS_TWICE_MESSAGE);
        }

        if (approved) {
            booking.setStatus(Status.APPROVED);
            log.info("Изменен статус бронирования с id = {} на {}", bookingId, Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
            log.info("Изменен статус бронирования с id = {} на {}", bookingId, Status.REJECTED);
        }

        return BookingMapper.toBookingDto(bookingStorage.save(booking));
    }

    @Override
    public BookingDto getBooking(long userId, long bookingId) {
        Booking booking = getBookingById(bookingId);

        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getUser().getId().equals(userId)) {
            log.warn("Выполнена попытка получить бронирование с id = {} пользователем с id = {}, который не является " +
                    "автором бронирования или владельцем вещи", bookingId, userId);
            throw new SecurityException(Constants.NOT_BOOKING_OR_ITEM_OWNER_CANNOT_GET_BOOKING_MESSAGE);
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getBookingsByBookerId(long userId, String state) {
        List<Booking> bookings;
        SearchingState searchingState = getSearchingState(state);

        checkUserExistence(userId);

        switch (searchingState) {
            case ALL:
                bookings = bookingStorage.findByBooker_IdOrderByStartDesc(userId);
                break;
            case PAST:
                bookings = bookingStorage.findByBooker_IdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
                break;
            case FUTURE:
                bookings = bookingStorage.findByBooker_IdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
                break;
            case CURRENT:
                bookings = bookingStorage.findByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(userId,
                        LocalDateTime.now(), LocalDateTime.now());
                break;
            case WAITING:
                bookings = bookingStorage.findByBooker_IdAndStatusOrderByStartDesc(userId, Status.WAITING);
                break;
            case REJECTED:
                bookings = bookingStorage.findByBooker_IdAndStatusOrderByStartDesc(userId, Status.REJECTED);
                break;
            default:
                throw new IllegalArgumentException(String.format(Constants.UNKNOWN_SEARCHING_STATE_MESSAGE, state));
        }

        log.info("Получен список бронирований автора с id = {} со статусом {} длиной {}", userId, state,
                bookings.size());
        return BookingMapper.toBookingDto(bookings);
    }

    @Override
    public List<BookingDto> getBookingsByOwnerId(long userId, String state) {
        List<Booking> bookings;
        SearchingState searchingState = getSearchingState(state);

        checkUserExistence(userId);

        switch (searchingState) {
            case ALL:
                bookings = bookingStorage.findByItem_User_IdOrderByStartDesc(userId);
                break;
            case REJECTED:
                bookings = bookingStorage.findByItem_User_IdAndStatusOrderByStartDesc(userId, Status.REJECTED);
                break;
            case WAITING:
                bookings = bookingStorage.findByItem_User_IdAndStatusOrderByStartDesc(userId, Status.WAITING);
                break;
            case FUTURE:
                bookings = bookingStorage.findByItem_User_IdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
                break;
            case PAST:
                bookings = bookingStorage.findByItem_User_IdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
                break;
            case CURRENT:
                bookings = bookingStorage.findByItem_User_IdAndStartBeforeAndEndAfterOrderByStartDesc(userId,
                        LocalDateTime.now(), LocalDateTime.now());
                break;
            default:
                throw new IllegalArgumentException(String.format(Constants.UNKNOWN_SEARCHING_STATE_MESSAGE, state));
        }

        log.info("Получен список бронирований владельца вещей с id = {} со статусом {} длиной {}", userId, state,
                bookings.size());
        return BookingMapper.toBookingDto(bookings);
    }

    private Booking getBookingById(long bookingId) {
        Optional<Booking> booking = bookingStorage.findById(bookingId);

        if (booking.isEmpty()) {
            log.warn("Выполнена попытка получить бронирование по несуществующему id = {}", bookingId);
            throw new NotFoundException(String.format(Constants.BOOKING_NOT_FOUND_MESSAGE, bookingId));
        }

        return booking.get();
    }

    private void checkTimeCorrectness(LocalDateTime start, LocalDateTime end) {
        if (!start.isBefore(end)) {
            log.warn("Выполнена попытка создать бронирование с некорректными датами: start = {}, end = {}", start, end);
            throw new IllegalArgumentException(Constants.WRONG_START_AND_END_BOOKING_DATES_MESSAGE);
        }
    }

    private void checkUserExistence(long userId) {
        if (!userStorage.existsById(userId)) {
            log.warn("Выполнена попытка получить бронирования несуществующего пользователя с id = {}", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        }
    }

    private SearchingState getSearchingState(String state) {
        try {
            return SearchingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            log.warn("Выполнена попытка получить список бронирований по несуществующему статусу {}", state);
            throw new IllegalArgumentException(String.format(Constants.UNKNOWN_SEARCHING_STATE_MESSAGE, state));
        }
    }
}
