package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Long, Map<Long, Item>> items;
    private long currentId;

    public InMemoryItemStorage() {
        items = new HashMap<>();
    }

    @Override
    public Item create(Item item) {
        if (!items.containsKey(item.getOwner())) {
            items.put(item.getOwner(), new HashMap<>());
        }

        item.setId(++currentId);
        items.get(item.getOwner()).put(item.getId(), item);

        log.info("Создана вещь с id = {}", item.getId());
        return item;
    }

    @Override
    public boolean isItemExist(long userId, long itemId) {
        log.info("Получена информация о существовании вещи с id = {} у пользователя с id = {}", itemId, userId);

        if (items.containsKey(userId)) {
            return items.get(userId).containsKey(itemId);
        } else {
            return false;
        }
    }

    @Override
    public boolean isItemExist(long itemId) {
        log.info("Получена информация о существовании вещи с id = {}", itemId);
        return items.values().stream().anyMatch(itemsMap -> itemsMap.containsKey(itemId));
    }

    @Override
    public Item get(long userId, long itemId) {
        Item item = items.get(userId).get(itemId);
        log.info("Получена вещь с id = {} у пользователя с id = {}", itemId, userId);

        return item;
    }

    @Override
    public Item get(long itemId) {
        for (Map<Long, Item> itemsMap : items.values()) {
            if (itemsMap.containsKey(itemId)) {
                log.info("Получена вещь с с id = {}", itemId);
                return itemsMap.get(itemId);
            }
        }

        return null;
    }

    @Override
    public Item update(Item item) {
        items.get(item.getOwner()).put(item.getId(), item);
        log.info("Обновлена информация о вещи с id = {} у пользователя с id = {}", item.getId(), item.getOwner());

        return item;
    }

    @Override
    public List<Item> getUsersItems(long userId) {
        List<Item> usersItems = new ArrayList<>(items.get(userId).values());
        log.info("Получен список вещей пользователя с id = {} длиной {}", userId, usersItems.size());

        return usersItems;
    }

    @Override
    public List<Item> searchItems(String text) {
        List<Item> searchedItems = new ArrayList<>();

        for (Map<Long, Item> itemsMap : items.values()) {
            for (Item item : itemsMap.values()) {
                if (Boolean.TRUE.equals(item.getAvailable()) &&
                        (item.getName().toLowerCase().contains(text.toLowerCase())
                                || item.getDescription().toLowerCase().contains(text.toLowerCase()))) {
                    searchedItems.add(item);
                }
            }
        }

        log.info("Получен список вещей длиной {}, найденный по поисковой строке: {}", searchedItems.size(), text);
        return searchedItems;
    }
}
