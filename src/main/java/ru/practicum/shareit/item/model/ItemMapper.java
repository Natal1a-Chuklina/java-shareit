package ru.practicum.shareit.item.model;

import ru.practicum.shareit.item.dto.ItemDto;

public class ItemMapper {
    private ItemMapper() {
    }

    public static Item toItem(ItemDto itemDto, long userId) {
        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                userId
        );
    }

    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable()
        );
    }
}
