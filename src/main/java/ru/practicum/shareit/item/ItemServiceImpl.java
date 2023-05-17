package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingStorage;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.CommentMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemMapper;
import ru.practicum.shareit.user.UserStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.utils.Constants;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static ru.practicum.shareit.utils.Constants.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final BookingStorage bookingStorage;
    private final CommentStorage commentStorage;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ItemDto createItem(long userId, ItemDto itemDto) {
        itemDto.setId(0);

        if (!userStorage.existsById(userId)) {
            log.warn("Выполнена попытка использовать несуществующий id пользователя при создании вещи: {}", userId);
            throw new NotFoundException(String.format(USER_NOT_FOUND_MESSAGE, userId));
        }

        User userRef = userStorage.getReferenceById(userId);
        Item savedItem = itemStorage.save(ItemMapper.toItem(itemDto, userRef));
        log.info("Создана вещь с id = {} у пользователя с id = {}", savedItem.getId(), userId);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ItemDto updateItem(long userId, ItemDto itemDto) {
        Optional<Item> currentItem = itemStorage.findById(itemDto.getId());

        if (currentItem.isEmpty()) {
            log.warn("Выполнена попытка обновить вещь по несуществующему id = {}", itemDto.getId());
            throw new NotFoundException(String.format(ITEM_NOT_FOUND_MESSAGE, itemDto.getId()));
        }

        if (!currentItem.get().getUser().getId().equals(userId)) {
            log.warn("Выполнена попытка обновить вещь с id = {}, которой нет у пользователя с id = {}",
                    itemDto.getId(), userId);
            throw new NotFoundException(String.format(USERS_ITEM_NOT_FOUND_MESSAGE, itemDto.getId(), userId));
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            currentItem.get().setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            currentItem.get().setDescription(itemDto.getDescription());
        }

        //тут ругается, что выражение всегда true, но это не так
        if (itemDto.getAvailable() != null) {
            currentItem.get().setAvailable(itemDto.getAvailable());
        }

        log.info("Обновлена информация о вещи с id = {} у пользователя с id = {}", itemDto.getId(), userId);
        return ItemMapper.toItemDto(itemStorage.save(currentItem.get()));
    }

    @Override
    public ItemWithBookingDto getItem(long itemId, long userId) {
        if (!userStorage.existsById(userId)) {
            log.warn("Выполнена попытка получить вещь несуществующим пользователем с id = {}", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        }

        Optional<Item> item = itemStorage.findById(itemId);
        Booking lastBooking = null;
        Booking nextBooking = null;

        if (item.isEmpty()) {
            log.warn("Выполнена попытка получить вещь по несуществующему id = {}", itemId);
            throw new NotFoundException(String.format(ITEM_NOT_FOUND_MESSAGE, itemId));
        }

        if (item.get().getUser().getId().equals(userId)) {
            List<Booking> sortedBookings = bookingStorage.findByItem_IdAndItem_User_IdAndStatusOrderByStartAsc(itemId,
                    userId, Status.APPROVED);

            Booking[] bookings = findLastAndNextBookings(sortedBookings);
            lastBooking = bookings[0];
            nextBooking = bookings[1];
        }

        List<Comment> comments = commentStorage.findByItem_IdOrderByIdAsc(itemId);
        log.info("Получен список отзывов на вещь с id = {} длиной {}", itemId, comments.size());
        log.info("Получена вещь с с id = {}", itemId);
        return ItemMapper.toItemWithBookingDto(item.get(), lastBooking, nextBooking, comments);
    }

    @Override
    public List<ItemWithBookingDto> getUsersItems(long userId) {
        List<Item> items = itemStorage.findByUser_IdOrderByIdAsc(userId);
        List<ItemWithBookingDto> mappedItems = new ArrayList<>();

        for (Item item : items) {
            List<Booking> sortedBookings = bookingStorage.findByItem_IdAndItem_User_IdAndStatusOrderByStartAsc(
                    item.getId(), userId, Status.APPROVED);

            Booking[] bookings = findLastAndNextBookings(sortedBookings);

            List<Comment> comments = commentStorage.findByItem_IdOrderByIdAsc(item.getId());
            log.info("Получен список отзывов на вещь с id = {} длиной {}", item.getId(), comments.size());

            mappedItems.add(ItemMapper.toItemWithBookingDto(item, bookings[0], bookings[1], comments));
        }

        log.info("Получен список вещей пользователя с id = {} длиной {}", userId, items.size());
        return mappedItems;
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }

        List<Item> searchedItems = itemStorage
                .findByDescriptionContainingAndAvailableTrueOrNameContainingAndAvailableTrueAllIgnoreCase(text, text);

        log.info("Получен список вещей длиной {}, найденный по поисковой строке: {}", searchedItems.size(), text);
        return ItemMapper.toItemDto(searchedItems);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public CommentDto createComment(CommentDto commentDto, long userId, long itemId) {
        if (!bookingStorage.existsByItem_IdAndBooker_IdAndStatusAndEndBefore(itemId, userId, Status.APPROVED,
                LocalDateTime.now())) {
            log.warn("Выполнена попытка оставить отзыв на вещь с id = {} пользователем с id = {}, который либо не брал" +
                    " вещь в аренду, либо срок аренды еще не истек", itemId, userId);
            throw new IllegalArgumentException(USER_CANNOT_LEAVE_COMMENT_MESSAGE);
        }

        if (commentStorage.existsByItem_IdAndAuthor_Id(itemId, userId)) {
            log.warn("Выполнена попытка повторно оставить отзыв на вещь с id = {} пользователем с id = {}",
                    itemId, userId);
            throw new AlreadyExistException(USER_CANNOT_LEAVE_COMMENT_TWICE_MESSAGE);
        }

        User userRef = userStorage.getReferenceById(userId);
        Item itemRef = itemStorage.getReferenceById(itemId);

        Comment comment = commentStorage.save(CommentMapper.toComment(commentDto, itemRef, userRef));
        log.info("Добавлен отзыв с id = {} на вещь с id = {} пользователем с id = {}", comment.getId(), itemId, userId);
        return CommentMapper.toCommentDto(comment);
    }

    private Booking[] findLastAndNextBookings(List<Booking> sortedBookings) {
        Booking lastBooking = null;
        Booking nextBooking = null;

        if (!sortedBookings.isEmpty()) {
            int nextBookingNumber = findNextBookingNumber(sortedBookings, LocalDateTime.now());

            if (nextBookingNumber == -1) {
                lastBooking = sortedBookings.get(sortedBookings.size() - 1);
            } else if (nextBookingNumber == 0) {
                nextBooking = sortedBookings.get(nextBookingNumber);
            } else {
                nextBooking = sortedBookings.get(nextBookingNumber);
                lastBooking = sortedBookings.get(nextBookingNumber - 1);
            }
        } else {
            return new Booking[]{null, null};
        }

        return new Booking[]{lastBooking, nextBooking};
    }

    private int findNextBookingNumber(List<Booking> sortedBookings, LocalDateTime time) {
        int number = -1;

        for (int i = 0; i < sortedBookings.size(); i++) {
            if (sortedBookings.get(i).getStart().isAfter(time)) {
                number = i;
                break;
            }
        }

        return number;
    }
}
