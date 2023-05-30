package ru.practicum.shareit.item.dto;

import lombok.*;
import ru.practicum.shareit.booking.dto.SimpleBookingDto;

import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ItemWithBookingDto {
    private long id;
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
    private SimpleBookingDto lastBooking;
    private SimpleBookingDto nextBooking;
    private List<CommentDto> comments;
}
