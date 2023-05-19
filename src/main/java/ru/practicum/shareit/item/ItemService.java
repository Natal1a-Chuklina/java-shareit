package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;

import java.util.List;

public interface ItemService {
    ItemDto createItem(long userId, ItemDto itemDto);

    ItemDto updateItem(long userId, ItemDto itemDto);

    ItemWithBookingDto getItem(long itemId, long userId);

    List<ItemWithBookingDto> getUsersItems(long userId, int from, int size);

    List<ItemDto> searchItems(String text, int from, int size);

    CommentDto createComment(CommentDto commentDto, long userId, long itemId);
}
