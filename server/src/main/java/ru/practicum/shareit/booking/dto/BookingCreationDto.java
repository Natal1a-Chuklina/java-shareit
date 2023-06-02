package ru.practicum.shareit.booking.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreationDto {
    private Long itemId;
    private LocalDateTime start;
    private LocalDateTime end;
}
