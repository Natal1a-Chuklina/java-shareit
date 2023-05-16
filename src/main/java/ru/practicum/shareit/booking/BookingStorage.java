package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;

@Transactional(readOnly = true)
public interface BookingStorage extends JpaRepository<Booking, Long> {
    List<Booking> findByBooker_IdOrderByStartDesc(Long bookerId);

    List<Booking> findByBooker_IdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime time);

    List<Booking> findByBooker_IdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime time);

    List<Booking> findByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(Long bookerId, LocalDateTime time1,
                                                                           LocalDateTime time2);

    List<Booking> findByBooker_IdAndStatusOrderByStartDesc(Long bookerId, Status status);

    List<Booking> findByItem_User_IdOrderByStartDesc(Long ownerId);

    List<Booking> findByItem_User_IdAndStatusOrderByStartDesc(Long ownerId, Status status);

    List<Booking> findByItem_User_IdAndStartAfterOrderByStartDesc(Long ownerId, LocalDateTime time);

    List<Booking> findByItem_User_IdAndEndBeforeOrderByStartDesc(Long ownerId, LocalDateTime time);

    List<Booking> findByItem_User_IdAndStartBeforeAndEndAfterOrderByStartDesc(Long ownerId, LocalDateTime time1,
                                                                              LocalDateTime time2);

    List<Booking> findByItem_IdAndItem_User_IdAndStatusOrderByStartAsc(Long itemId, Long ownerId, Status status);

    boolean existsByItem_IdAndBooker_IdAndStatusAndEndBefore(Long itemId, Long userId, Status status, LocalDateTime end);

    List<Booking> findByItem_IdAndEndAfterAndStatusOrderByStartAsc(Long itemId, LocalDateTime end, Status status);

}
