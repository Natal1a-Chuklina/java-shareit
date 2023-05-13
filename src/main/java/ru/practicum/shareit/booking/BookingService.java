package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto createBooking(BookingCreationDto bookingDto, long userId);

    BookingDto setBookingStatus(long userId, long bookingId, boolean approved);

    BookingDto getBooking(long userId, long bookingId);

    List<BookingDto> getBookingsByBookerId(long userId, String state);

    List<BookingDto> getBookingsByOwnerId(long userId, String state);
}
