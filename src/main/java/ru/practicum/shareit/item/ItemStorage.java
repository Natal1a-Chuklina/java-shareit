package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item create(Item item);

    boolean isItemExist(long userId, long itemId);

    boolean isItemExist(long itemId);

    Item get(long userId, long itemId);

    Item get(long itemId);

    Item update(Item item);

    List<Item> getUsersItems(long userId);

    List<Item> searchItems(String text);
}
