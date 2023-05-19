package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;

@Transactional(readOnly = true)
public interface BookingStorage extends JpaRepository<Booking, Long> {
    Page<Booking> findByBooker_Id(Long bookerId, Pageable pageable);

    Page<Booking> findByBooker_IdAndEndBefore(Long bookerId, LocalDateTime time, Pageable pageable);

    Page<Booking> findByBooker_IdAndStartAfter(Long bookerId, LocalDateTime time, Pageable pageable);

    Page<Booking> findByBooker_IdAndStartBeforeAndEndAfter(Long bookerId, LocalDateTime time1,
                                                           LocalDateTime time2, Pageable pageable);

    Page<Booking> findByBooker_IdAndStatus(Long bookerId, Status status, Pageable pageable);

    Page<Booking> findByItem_User_Id(Long ownerId, Pageable pageable);

    Page<Booking> findByItem_User_IdAndStatus(Long ownerId, Status status, Pageable pageable);

    Page<Booking> findByItem_User_IdAndStartAfter(Long ownerId, LocalDateTime time, Pageable pageable);

    Page<Booking> findByItem_User_IdAndEndBefore(Long ownerId, LocalDateTime time, Pageable pageable);

    Page<Booking> findByItem_User_IdAndStartBeforeAndEndAfter(Long ownerId, LocalDateTime time1,
                                                              LocalDateTime time2, Pageable pageable);

    List<Booking> findByItem_IdAndItem_User_IdAndStatusOrderByStartAsc(Long itemId, Long ownerId, Status status);

    boolean existsByItem_IdAndBooker_IdAndStatusAndEndBefore(Long itemId, Long userId, Status status, LocalDateTime end);

    List<Booking> findByItem_IdAndEndAfterAndStatusOrderByStartAsc(Long itemId, LocalDateTime end, Status status);

}
