package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto createItem(long userId, ItemDto itemDto);

    ItemDto updateItem(long userId, ItemDto itemDto);

    ItemDto getItem(long itemId);

    List<ItemDto> getUsersItems(long userId);

    List<ItemDto> searchItems(String text);
}
