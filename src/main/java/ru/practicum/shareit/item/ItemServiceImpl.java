package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemMapper;
import ru.practicum.shareit.user.UserStorage;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.utils.Constants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public ItemDto createItem(long userId, ItemDto itemDto) {
        checkUserExistence(userId);
        return ItemMapper.toItemDto(itemStorage.create(ItemMapper.toItem(itemDto, userId)));
    }

    /* Тут получилась небольшая избыточность, так как после обновления currentItem непустыми полями из itemDto
    в ItemStorage уже находится актуальная версия Item и вызов метода update ничего не изменит, но при добавлении бд
    этой избыточности не будет. */
    @Override
    public ItemDto updateItem(long userId, ItemDto itemDto) {
        checkUserExistence(userId);
        checkItemExistence(userId, itemDto.getId());

        Item currentItem = itemStorage.get(userId, itemDto.getId());

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            currentItem.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            currentItem.setDescription(itemDto.getDescription());
        }

        //тут ругается, что выражение всегда true, но это не так
        if (itemDto.getAvailable() != null) {
            currentItem.setAvailable(itemDto.getAvailable());
        }

        return ItemMapper.toItemDto(itemStorage.update(currentItem));
    }

    @Override
    public ItemDto getItem(long itemId) {
        checkItemExistence(itemId);

        return ItemMapper.toItemDto(itemStorage.get(itemId));
    }

    @Override
    public List<ItemDto> getUsersItems(long userId) {
        checkUserExistence(userId);

        return itemStorage.getUsersItems(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }

        return itemStorage.searchItems(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private void checkUserExistence(long userId) {
        if (!userStorage.isUserExist(userId)) {
            log.warn("Выполнена попытка использовать несуществующий id пользователя: {}", userId);
            throw new NotFoundException(String.format(USER_NOT_FOUND_MESSAGE, userId));
        }
    }

    private void checkItemExistence(long userId, long itemId) {
        if (!itemStorage.isItemExist(userId, itemId)) {
            log.warn("Выполнена попытка получить вещь с id = {}, которой нет у пользователя с id = {}", itemId, userId);
            throw new NotFoundException(String.format(USERS_ITEM_NOT_FOUND_MESSAGE, itemId, userId));
        }
    }

    private void checkItemExistence(long itemId) {
        if (!itemStorage.isItemExist(itemId)) {
            log.warn("Выполнена попытка получить вещь по несуществующему id = {}", itemId);
            throw new NotFoundException(String.format(ITEM_NOT_FOUND_MESSAGE, itemId));
        }
    }
}
