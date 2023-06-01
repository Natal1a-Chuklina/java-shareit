package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.SearchingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingMapper;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotAvailableException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemStorage;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserStorage;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static ru.practicum.shareit.utils.Constants.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd.MM.yy");
    private final BookingStorage bookingStorage;
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public BookingDto createBooking(BookingCreationDto bookingDto, long userId) {
        Optional<User> user = userStorage.findById(userId);
        if (user.isEmpty()) {
            log.warn("Выполнена попытка забронировать вещь пользователем с несуществующим id = {}", userId);
            throw new NotFoundException(String.format(USER_NOT_FOUND_MESSAGE, userId));
        }

        Optional<Item> item = itemStorage.findById(bookingDto.getItemId());
        if (item.isEmpty()) {
            log.warn("Выполнена попытка забронировать вещь с несуществующим id = {} пользователем с id = {}",
                    bookingDto.getItemId(), userId);
            throw new NotFoundException(String.format(ITEM_NOT_FOUND_MESSAGE, bookingDto.getItemId()));
        }

        if (Boolean.FALSE.equals(item.get().getAvailable())) {
            log.warn("Выполнена попытка пользователем с id = {} забронировать вещь с id = {}, которая" +
                    " недоступна к бронированию", userId, bookingDto.getItemId());
            throw new NotAvailableException(String.format(ITEM_NOT_AVAILABLE_MESSAGE,
                    bookingDto.getItemId()));
        }

        if (item.get().getUser().getId().equals(userId)) {
            log.warn("Выполнена попытка пользователем с id = {} забронировать свою вещь с id = {}",
                    userId, item.get().getId());
            throw new SecurityException(USER_CANNOT_BOOK_HIS_ITEM_MESSAGE);
        }

        checkTimeCrossings(bookingDto.getStart(), bookingDto.getEnd(), item.get().getId());

        Booking createdBooking = bookingStorage.save(BookingMapper.toBooking(bookingDto, user.get(), item.get()));
        log.info("Создано бронирование с id = {} пользователем с id = {} на вещь с id = {}", createdBooking.getId(),
                userId, bookingDto.getItemId());
        return BookingMapper.toBookingDto(createdBooking);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public BookingDto setBookingStatus(long userId, long bookingId, boolean approved) {
        Booking booking = getBookingById(bookingId);

        if (!itemStorage.existsByUser_IdAndId(userId, booking.getItem().getId())) {
            log.warn("Выполнена попытка изменить статус бронирования вещи с id = {} пользователем с id = {}, который " +
                    "не является ее владельцем", booking.getItem().getId(), userId);
            throw new SecurityException(NOT_OWNER_CANNOT_CHANGE_BOOKING_STATUS_MESSAGE);
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
            throw new SecurityException(NOT_BOOKING_OR_ITEM_OWNER_CANNOT_GET_BOOKING_MESSAGE);
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getBookingsByBookerId(long userId, SearchingState state, int from, int size) {
        Sort sortByStartDate = Sort.by(Sort.Direction.DESC, "start");
        Pageable page = PageRequest.of(from / size, size, sortByStartDate);
        Page<Booking> bookings;

        checkUserExistence(userId);

        switch (state) {
            case ALL:
                bookings = bookingStorage.findByBooker_Id(userId, page);
                break;
            case PAST:
                bookings = bookingStorage.findByBooker_IdAndEndBefore(userId, LocalDateTime.now(), page);
                break;
            case FUTURE:
                bookings = bookingStorage.findByBooker_IdAndStartAfter(userId, LocalDateTime.now(), page);
                break;
            case CURRENT:
                LocalDateTime currentTime = LocalDateTime.now();
                bookings = bookingStorage.findByBooker_IdAndStartBeforeAndEndAfter(userId, currentTime, currentTime,
                        page);
                break;
            case WAITING:
                bookings = bookingStorage.findByBooker_IdAndStatus(userId, Status.WAITING, page);
                break;
            case REJECTED:
                bookings = bookingStorage.findByBooker_IdAndStatus(userId, Status.REJECTED, page);
                break;
            default:
                throw new IllegalArgumentException(String.format(UNKNOWN_SEARCHING_STATE_MESSAGE, state));
        }

        log.info("Получен список бронирований автора с id = {} со статусом {} длиной {}", userId, state,
                bookings.getContent().size());
        return BookingMapper.toBookingDto(bookings.getContent());
    }

    @Override
    public List<BookingDto> getBookingsByOwnerId(long userId, SearchingState state, int from, int size) {
        Sort sortByStartDate = Sort.by(Sort.Direction.DESC, "start");
        Pageable page = PageRequest.of(from / size, size, sortByStartDate);
        Page<Booking> bookings;

        checkUserExistence(userId);

        switch (state) {
            case ALL:
                bookings = bookingStorage.findByItem_User_Id(userId, page);
                break;
            case REJECTED:
                bookings = bookingStorage.findByItem_User_IdAndStatus(userId, Status.REJECTED, page);
                break;
            case WAITING:
                bookings = bookingStorage.findByItem_User_IdAndStatus(userId, Status.WAITING, page);
                break;
            case FUTURE:
                bookings = bookingStorage.findByItem_User_IdAndStartAfter(userId, LocalDateTime.now(), page);
                break;
            case PAST:
                bookings = bookingStorage.findByItem_User_IdAndEndBefore(userId, LocalDateTime.now(), page);
                break;
            case CURRENT:
                bookings = bookingStorage.findByItem_User_IdAndStartBeforeAndEndAfter(userId,
                        LocalDateTime.now(), LocalDateTime.now(), page);
                break;
            default:
                throw new IllegalArgumentException(String.format(UNKNOWN_SEARCHING_STATE_MESSAGE, state));
        }

        log.info("Получен список бронирований владельца вещей с id = {} со статусом {} длиной {}", userId, state,
                bookings.getContent().size());
        return BookingMapper.toBookingDto(bookings.getContent());
    }

    private Booking getBookingById(long bookingId) {
        Optional<Booking> booking = bookingStorage.findById(bookingId);

        if (booking.isEmpty()) {
            log.warn("Выполнена попытка получить бронирование по несуществующему id = {}", bookingId);
            throw new NotFoundException(String.format(BOOKING_NOT_FOUND_MESSAGE, bookingId));
        }

        return booking.get();
    }

    private void checkUserExistence(long userId) {
        if (!userStorage.existsById(userId)) {
            log.warn("Выполнена попытка получить бронирования несуществующего пользователя с id = {}", userId);
            throw new NotFoundException(String.format(USER_NOT_FOUND_MESSAGE, userId));
        }
    }

    private void checkTimeCrossings(LocalDateTime bookingStart, LocalDateTime bookingEnd, long itemId) {
        List<Booking> bookings = bookingStorage.findByItem_IdAndEndAfterAndStatusOrderByStartAsc(itemId,
                LocalDateTime.now(), Status.APPROVED);

        for (Booking booking : bookings) {
            if ((bookingStart.isAfter(booking.getStart()) && bookingStart.isBefore(booking.getEnd()))
                    || (bookingStart.isBefore(booking.getStart()) && bookingEnd.isAfter(booking.getStart()))) {
                log.warn("Выполнена попытка создать бронирование вещи с id = {}, пересекающееся по времени с уже" +
                        " подтвержденным бронированием с id = {}", itemId, booking.getId());
                throw new AlreadyExistException(String.format(TIME_NOT_AVAILABLE_FOR_BOOKING_MESSAGE,
                        bookingStart.format(FORMATTER), bookingEnd.format(FORMATTER)));
            }
        }
    }
}
