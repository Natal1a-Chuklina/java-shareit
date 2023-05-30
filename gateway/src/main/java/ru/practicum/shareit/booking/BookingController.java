package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.SearchingState;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

import static ru.practicum.shareit.utils.Constants.*;


@RestController
@RequestMapping(path = "/bookings")
@Slf4j
@RequiredArgsConstructor
@Validated
public class BookingController {
    private static final String DEFAULT_SEARCH_VALUE = "ALL";
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> createBooking(@Valid @RequestBody BookingCreationDto bookingDto,
                                                @RequestHeader(HEADER_WITH_USER_ID_NAME) @Positive long userId) {
        log.info("Попытка забронировать вещь с id = {} пользователем с id = {}", bookingDto.getItemId(), userId);
        checkTimeCorrectness(bookingDto.getStart(), bookingDto.getEnd());
        return bookingClient.createBooking(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> setBookingStatus(@PathVariable @Positive long bookingId, @RequestParam boolean approved,
                                                   @RequestHeader(HEADER_WITH_USER_ID_NAME) @Positive long userId) {
        log.info("Попытка изменить статус бронирования с id = {} пользователем с id = {}", bookingId, userId);
        return bookingClient.setBookingStatus(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@PathVariable @Positive long bookingId,
                                             @RequestHeader(HEADER_WITH_USER_ID_NAME) @Positive long userId) {
        log.info("Попытка получить бронирование по id = {} пользователем с id = {}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getBookingsByBookerId(@RequestHeader(HEADER_WITH_USER_ID_NAME) @Positive long userId,
                                                        @RequestParam(defaultValue = DEFAULT_SEARCH_VALUE) String state,
                                                        @RequestParam(defaultValue = DEFAULT_START_PAGE) @Min(0) int from,
                                                        @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) @Positive int size) {
        log.info("Попытка получить {} бронирований начиная с {} со статусом {} автора бронирований с id = {}", size,
                from, state, userId);
        return bookingClient.getBookingsByBookerId(userId, getSearchingState(state), from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsByOwnerId(@RequestHeader(HEADER_WITH_USER_ID_NAME) @Positive long userId,
                                                       @RequestParam(defaultValue = DEFAULT_SEARCH_VALUE) String state,
                                                       @RequestParam(defaultValue = DEFAULT_START_PAGE) @Min(0) int from,
                                                       @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) @Positive int size) {
        log.info("Попытка получить {} бронирований начиная с {} со статусом {} владельца вещей с id = {}", size, from,
                state, userId);
        return bookingClient.getBookingsByOwnerId(userId, getSearchingState(state), from, size);
    }

    private void checkTimeCorrectness(LocalDateTime start, LocalDateTime end) {
        if (!start.isBefore(end)) {
            log.warn("Выполнена попытка создать бронирование с некорректными датами: start = {}, end = {}", start, end);
            throw new IllegalArgumentException(WRONG_START_AND_END_BOOKING_DATES_MESSAGE);
        }
    }

    private SearchingState getSearchingState(String state) {
        try {
            return SearchingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            log.warn("Выполнена попытка получить список бронирований по несуществующему статусу {}", state);
            throw new IllegalArgumentException(String.format(UNKNOWN_SEARCHING_STATE_MESSAGE, state));
        }
    }
}
