package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingStorage;
import ru.practicum.shareit.booking.dto.SimpleBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestStorage;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.utils.Constants;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @Mock
    private ItemStorage itemStorage;
    @Mock
    private UserStorage userStorage;
    @Mock
    private BookingStorage bookingStorage;
    @Mock
    private CommentStorage commentStorage;
    @Mock
    private ItemRequestStorage itemRequestStorage;
    @InjectMocks
    private ItemServiceImpl itemService;
    @Captor
    private ArgumentCaptor<Item> itemArgumentCaptor;
    @Captor
    private ArgumentCaptor<Comment> commentArgumentCaptor;

    @Test
    void createItem_WhenUserDoesNotExist_ThenThrowsNotFoundException() {
        ItemDto itemDto = new ItemDto(0, "name", "description", true, null);
        long userId = 1;
        when(userStorage.existsById(userId)).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class)
                .as("Проверка создания вещи пользователем, id которого нет в базе")
                .isThrownBy(() -> itemService.createItem(userId, itemDto))
                .withMessage(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));

        verify(userStorage, Mockito.times(1)).existsById(userId);
        Mockito.verifyNoInteractions(itemStorage);
    }

    @Test
    void createItem_WhenRequestWithSuchIdDoesNotExist_ThenTrowsNotFoundException() {
        long requestId = 1;
        long userId = 1;
        ItemDto itemDto = new ItemDto(1, "name", "description", true, requestId);
        when(userStorage.existsById(userId)).thenReturn(true);
        when(itemRequestStorage.existsById(requestId)).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class)
                .as("Проверка создания вещи по запросу, id которого нет в базе")
                .isThrownBy(() -> itemService.createItem(userId, itemDto))
                .withMessage(String.format(Constants.REQUEST_NOT_FOUND_MESSAGE, requestId));

        verify(itemRequestStorage, Mockito.times(1)).existsById(requestId);
        Mockito.verifyNoInteractions(itemStorage);
    }

    @Test
    void createItem_WhenItemDtoDoesNotContainRequestId_ThenItemCreated() {
        long userId = 1;
        ItemDto inputItemDto = new ItemDto(1, "name", "description", true, null);
        ItemDto expectedItemDto = new ItemDto(1, "name", "description", true, null);
        Item itemToSave = new Item(0L, "name", "description", true, new User(userId),
                null);
        when(userStorage.existsById(userId)).thenReturn(true);
        when(itemStorage.save(itemToSave))
                .thenReturn(new Item(1L, "name", "description", true, new User(1L,
                        "name", "mail@mail.ru"), null));
        when(userStorage.getReferenceById(userId)).thenReturn(new User(1L, null, null));

        assertThatCode(() -> {
            ItemDto actualDto = itemService.createItem(userId, inputItemDto);
            assertThat(actualDto)
                    .as("Проверка создания вещи при корректных входных данных, когда поле запроса не заполнено")
                    .isNotNull()
                    .isEqualTo(expectedItemDto);
        }).doesNotThrowAnyException();

        verify(itemStorage, Mockito.times(1)).save(itemArgumentCaptor.capture());
        assertThat(itemArgumentCaptor.getValue())
                .as("Проверка передаваемого аргумента в метод сохранения вещи")
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", itemToSave.getId())
                .hasFieldOrPropertyWithValue("name", itemToSave.getName())
                .hasFieldOrPropertyWithValue("description", itemToSave.getDescription())
                .hasFieldOrPropertyWithValue("available", itemToSave.getAvailable())
                .hasFieldOrPropertyWithValue("user", itemToSave.getUser())
                .hasFieldOrPropertyWithValue("itemRequest", itemToSave.getItemRequest());
        Mockito.verifyNoMoreInteractions(itemStorage);
    }

    @Test
    void createItem_WhenItemDtoContainsRequestId_ThenItemCreated() {
        long requestId = 1;
        long userId = 1;
        ItemDto inputItemDto = new ItemDto(1, "name", "description", true, requestId);
        ItemDto expectedItemDto = new ItemDto(1, "name", "description", true, requestId);
        Item itemToSave = new Item(0L, "name", "description", true, new User(userId),
                new ItemRequest(requestId, null, null, null));
        when(userStorage.existsById(userId)).thenReturn(true);
        when(itemRequestStorage.existsById(requestId)).thenReturn(true);
        when(itemStorage.save(itemToSave))
                .thenReturn(new Item(1L, "name", "description", true, new User(userId,
                        "name", "mail@mail.ru"), new ItemRequest(requestId, "description",
                        new User(2L), LocalDateTime.now().minusDays(1))));
        when(userStorage.getReferenceById(userId)).thenReturn(new User(userId, null, null));
        when(itemRequestStorage.getReferenceById(userId)).thenReturn(new ItemRequest(requestId, null,
                null, null));

        assertThatCode(() -> {
            ItemDto actualDto = itemService.createItem(userId, inputItemDto);
            assertThat(actualDto)
                    .as("Проверка создания вещи при корректных входных данных, когда поле запроса заполнено")
                    .isNotNull()
                    .isEqualTo(expectedItemDto);
        }).doesNotThrowAnyException();

        verify(itemStorage, Mockito.times(1)).save(itemArgumentCaptor.capture());
        assertThat(itemArgumentCaptor.getValue())
                .as("Проверка передаваемого аргумента в метод сохранения вещи")
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", itemToSave.getId())
                .hasFieldOrPropertyWithValue("name", itemToSave.getName())
                .hasFieldOrPropertyWithValue("description", itemToSave.getDescription())
                .hasFieldOrPropertyWithValue("available", itemToSave.getAvailable())
                .hasFieldOrPropertyWithValue("user", itemToSave.getUser())
                .hasFieldOrPropertyWithValue("itemRequest", itemToSave.getItemRequest());
        Mockito.verifyNoMoreInteractions(itemStorage);
    }

    @Test
    void updateItem_WhenItemDoesNotExists_ThenThrowsNotFoundException() {
        long itemId = 1;
        long userId = 1;
        ItemDto inputItemDto = new ItemDto(itemId, "new_name", "new_description", true,
                null);
        when(itemStorage.findById(itemId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .as("Проверка обновления вещи по id, которого нет в базе")
                .isThrownBy(() -> itemService.updateItem(userId, inputItemDto))
                .withMessage(String.format(Constants.ITEM_NOT_FOUND_MESSAGE, itemId));

        verify(itemStorage, Mockito.times(1)).findById(itemId);
        Mockito.verifyNoMoreInteractions(itemStorage);
    }

    @Test
    void updateItem_WhenUserNotOwnerOfTheItem_ThenThrowsNotFoundException() {
        long itemId = 1;
        long userId = 1;
        ItemDto inputItemDto = new ItemDto(itemId, "new_name", "new_description", true,
                null);
        Item oldItem = new Item(itemId, "old_name", "old_description", true,
                new User(userId + 1), null);
        when(itemStorage.findById(itemId)).thenReturn(Optional.of(oldItem));

        assertThatExceptionOfType(NotFoundException.class)
                .as("Проверка обновления вещи, id владельца которой не совпадает с переданным id")
                .isThrownBy(() -> itemService.updateItem(userId, inputItemDto))
                .withMessage(String.format(Constants.USERS_ITEM_NOT_FOUND_MESSAGE, itemId, userId));

        verify(itemStorage, Mockito.times(1)).findById(itemId);
        Mockito.verifyNoMoreInteractions(itemStorage);
    }

    @Test
    void updateItem_WhenDtoContainsNullFieldsAndFieldsNotAvailableToUpdate_ThenUpdatedWithOnlyCorrectFields() {
        String oldName = "old_name";
        String oldDescription = "old_description";
        boolean oldAvailable = true;
        long itemId = 1;
        long userId = 1;
        ItemDto inputItemDto = new ItemDto(itemId, null, null, null, 1L);
        ItemDto expectedItemDto = new ItemDto(itemId, oldName, oldDescription, oldAvailable, null);
        Item oldItem = new Item(itemId, oldName, oldDescription, oldAvailable, new User(userId), null);
        when(itemStorage.findById(itemId)).thenReturn(Optional.of(oldItem));
        when(itemStorage.save(oldItem)).thenReturn(oldItem);

        assertThatCode(() -> {
            ItemDto updatedItem = itemService.updateItem(userId, inputItemDto);
            assertThat(updatedItem)
                    .as("Проверка обновления вещи, когда поля для обновления null")
                    .isNotNull()
                    .isEqualTo(expectedItemDto);
        }).doesNotThrowAnyException();

        verify(itemStorage, Mockito.times(1)).save(itemArgumentCaptor.capture());
        assertThat(itemArgumentCaptor.getValue())
                .as("Проверка передаваемого аргумента в метод обновления вещи")
                .hasFieldOrPropertyWithValue("id", itemId)
                .hasFieldOrPropertyWithValue("name", oldName)
                .hasFieldOrPropertyWithValue("description", oldDescription)
                .hasFieldOrPropertyWithValue("available", oldAvailable)
                .hasFieldOrPropertyWithValue("itemRequest", null);
        verifyNoMoreInteractions(itemStorage);
    }

    @Test
    void updateItem_WhenDtoContainsEmptyFields_ThenUpdatedWithOnlyCorrectFields() {
        String oldName = "old_name";
        String oldDescription = "old_description";
        boolean newAvailable = false;
        long itemId = 1;
        long userId = 1;
        ItemDto inputItemDto = new ItemDto(itemId, "  ", "  ", false, 1L);
        ItemDto expectedItemDto = new ItemDto(itemId, oldName, oldDescription, newAvailable, null);
        Item oldItem = new Item(itemId, oldName, oldDescription, true, new User(userId), null);
        Item updatedItem = new Item(itemId, oldName, oldDescription, newAvailable, new User(userId), null);
        when(itemStorage.findById(itemId)).thenReturn(Optional.of(oldItem));
        when(itemStorage.save(oldItem)).thenReturn(updatedItem);

        assertThatCode(() -> {
            ItemDto actualItem = itemService.updateItem(userId, inputItemDto);
            assertThat(actualItem)
                    .as("Проверка обновления вещи, когда все поля для обновления пустые кроме available")
                    .isNotNull()
                    .isEqualTo(expectedItemDto);
        }).doesNotThrowAnyException();

        verify(itemStorage, Mockito.times(1)).save(itemArgumentCaptor.capture());
        assertThat(itemArgumentCaptor.getValue())
                .as("Проверка передаваемого аргумента в метод обновления вещи")
                .hasFieldOrPropertyWithValue("id", itemId)
                .hasFieldOrPropertyWithValue("name", oldName)
                .hasFieldOrPropertyWithValue("description", oldDescription)
                .hasFieldOrPropertyWithValue("available", newAvailable);
        verifyNoMoreInteractions(itemStorage);
    }

    @Test
    void updateItem_WhenDtoContainsNotEmptyFields_ThenUpdatedWithOnlyCorrectFields() {
        String newName = "new_name";
        String newDescription = "new_description";
        long itemId = 1;
        long userId = 1;
        ItemDto inputItemDto = new ItemDto(itemId, newName, newDescription, false, null);
        ItemDto expectedItemDto = new ItemDto(itemId, newName, newDescription, true, 1L);
        Item oldItem = new Item(itemId, "old_name", "old_description", true, new User(userId),
                new ItemRequest(1L, null, null, null));
        Item updatedItem = new Item(itemId, newName, newDescription, true, new User(userId),
                new ItemRequest(1L, null, null, null));
        when(itemStorage.findById(itemId)).thenReturn(Optional.of(oldItem));
        when(itemStorage.save(oldItem)).thenReturn(updatedItem);

        assertThatCode(() -> {
            ItemDto actualItem = itemService.updateItem(userId, inputItemDto);
            assertThat(actualItem)
                    .as("Проверка обновления вещи, когда не пустые и не null название и описание вещи")
                    .isNotNull()
                    .isEqualTo(expectedItemDto);
        }).doesNotThrowAnyException();

        verify(itemStorage, Mockito.times(1)).save(itemArgumentCaptor.capture());
        assertThat(itemArgumentCaptor.getValue())
                .as("Проверка передаваемого аргумента в метод обновления вещи")
                .hasFieldOrPropertyWithValue("id", itemId)
                .hasFieldOrPropertyWithValue("name", newName)
                .hasFieldOrPropertyWithValue("description", newDescription)
                .hasFieldOrPropertyWithValue("itemRequest",
                        new ItemRequest(1L, null, null, null));
        verifyNoMoreInteractions(itemStorage);
    }

    @Test
    void getItem_WhenUserDoesNotExist_ThenThrowsNotFoundException() {
        long itemId = 1;
        long userId = 1;
        when(userStorage.existsById(userId)).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class)
                .as("Проверка получения вещи, когда id пользователя не найден в базе")
                .isThrownBy(() -> itemService.getItem(userId, itemId))
                .withMessage(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        verify(userStorage, Mockito.times(1)).existsById(userId);
        Mockito.verifyNoInteractions(itemStorage);
    }

    @Test
    void getItem_WhenItemDoesNotExist_ThenThrowsNotFoundException() {
        long itemId = 1;
        long userId = 1;
        when(userStorage.existsById(userId)).thenReturn(true);
        when(itemStorage.findById(itemId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .as("Проверка получения вещи, когда id вещи не найден в базе")
                .isThrownBy(() -> itemService.getItem(userId, itemId))
                .withMessage(String.format(Constants.ITEM_NOT_FOUND_MESSAGE, itemId));
        verify(itemStorage, Mockito.times(1)).findById(itemId);
        verifyNoMoreInteractions(itemStorage);
    }

    @Test
    void getItem_WhenUserNotItemOwnerAndCommentsExist_ThenReturnItemWithComments() {
        long itemId = 1;
        long userId = 1;
        LocalDateTime time1 = LocalDateTime.now().minusDays(1);
        LocalDateTime time2 = LocalDateTime.now().minusDays(3);
        ItemWithBookingDto expectedItemDto = new ItemWithBookingDto(itemId, "name", "description",
                true, null, null, null,
                List.of(new CommentDto(1L, "text1", "name1", time1),
                        new CommentDto(2L, "text2", "name2", time2)));
        Item item = new Item(itemId, "name", "description", true, new User(userId + 1),
                null);
        when(userStorage.existsById(userId)).thenReturn(true);
        when(itemStorage.findById(itemId)).thenReturn(Optional.of(item));
        when(commentStorage.findByItem_IdOrderByIdAsc(itemId)).thenReturn(List.of(new Comment(1L, "text1", item,
                new User(userId + 2, "name1", "mail1@mail.ru"), time1), new Comment(2L, "text2",
                item, new User(userId + 3, "name2", "mail2@mail.ru"), time2)));

        assertThatCode(() -> {
            ItemWithBookingDto actualItemDto = itemService.getItem(itemId, userId);
            assertThat(actualItemDto)
                    .as("Проверка получения вещи по id не ее владельцем, когда комментарии не пусты")
                    .isNotNull()
                    .isEqualTo(expectedItemDto);
        }).doesNotThrowAnyException();

        verify(itemStorage, Mockito.times(1)).findById(itemId);
        verify(bookingStorage, Mockito.never()).findByItem_IdAndItem_User_IdAndStatusOrderByStartAsc(anyLong(),
                anyLong(), any(Status.class));
        verify(commentStorage, Mockito.times(1)).findByItem_IdOrderByIdAsc(itemId);
    }

    @Test
    void getItem_WhenUserItemOwnerWithNoBookings_ThenReturnItemWithNullBookings() {
        long itemId = 1;
        long userId = 1;
        ItemWithBookingDto expectedItemDto = new ItemWithBookingDto(itemId, "name", "description",
                true, null, null, null, Collections.emptyList());
        Item item = new Item(itemId, "name", "description", true, new User(userId),
                null);
        when(userStorage.existsById(userId)).thenReturn(true);
        when(itemStorage.findById(itemId)).thenReturn(Optional.of(item));

        assertThatCode(() -> {
            ItemWithBookingDto actualItemDto = itemService.getItem(itemId, userId);
            assertThat(actualItemDto)
                    .as("Проверка получения вещи по id ее владельцем, когда нет бронирований вещи")
                    .isNotNull()
                    .isEqualTo(expectedItemDto);
        }).doesNotThrowAnyException();

        verify(itemStorage, Mockito.times(1)).findById(itemId);
        verify(bookingStorage, Mockito.times(1)).findByItem_IdAndItem_User_IdAndStatusOrderByStartAsc(userId,
                itemId, Status.APPROVED);
        verify(commentStorage, Mockito.times(1)).findByItem_IdOrderByIdAsc(itemId);
    }

    @Test
    void getItem_WhenUserItemOwnerWithNoNextBookings_ThenReturnItemWithNullNextBooking() {
        long itemId = 1;
        long userId = 1;
        Item item = new Item(itemId, "name", "description", true, new User(userId),
                null);
        LocalDateTime start = LocalDateTime.now().minusDays(3);
        LocalDateTime end = LocalDateTime.now().minusDays(2);
        Booking booking1 = new Booking(1L, Status.APPROVED, item, new User(userId + 1), LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(4));
        Booking booking2 = new Booking(2L, Status.APPROVED, item, new User(userId + 2), start, end);
        ItemWithBookingDto expectedItemDto = new ItemWithBookingDto(itemId, "name", "description",
                true, null, new SimpleBookingDto(2L, start, end, Status.APPROVED,
                userId + 2), null, Collections.emptyList());
        when(userStorage.existsById(userId)).thenReturn(true);
        when(itemStorage.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingStorage.findByItem_IdAndItem_User_IdAndStatusOrderByStartAsc(itemId, userId, Status.APPROVED))
                .thenReturn(List.of(booking1, booking2));

        assertThatCode(() -> {
            ItemWithBookingDto actualItemDto = itemService.getItem(itemId, userId);
            assertThat(actualItemDto)
                    .as("Проверка получения вещи по id ее владельцем, когда нет бронирований вещи")
                    .isNotNull()
                    .isEqualTo(expectedItemDto);
        }).doesNotThrowAnyException();

        verify(itemStorage, Mockito.times(1)).findById(itemId);
        verify(bookingStorage, Mockito.times(1)).findByItem_IdAndItem_User_IdAndStatusOrderByStartAsc(userId,
                itemId, Status.APPROVED);
        verify(commentStorage, Mockito.times(1)).findByItem_IdOrderByIdAsc(itemId);
    }

    @Test
    void getItem_WhenUserItemOwnerWithNoLastBookings_ThenReturnItemWithNullLastBooking() {
        long itemId = 1;
        long userId = 1;
        Item item = new Item(itemId, "name", "description", true, new User(userId),
                null);
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        Booking booking1 = new Booking(1L, Status.APPROVED, item, new User(userId + 1), start, end);
        Booking booking2 = new Booking(2L, Status.APPROVED, item, new User(userId + 2),
                LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4));
        ItemWithBookingDto expectedItemDto = new ItemWithBookingDto(itemId, "name", "description",
                true, null, null, new SimpleBookingDto(1L, start, end, Status.APPROVED,
                userId + 1), Collections.emptyList());
        when(userStorage.existsById(userId)).thenReturn(true);
        when(itemStorage.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingStorage.findByItem_IdAndItem_User_IdAndStatusOrderByStartAsc(itemId, userId, Status.APPROVED))
                .thenReturn(List.of(booking1, booking2));

        assertThatCode(() -> {
            ItemWithBookingDto actualItemDto = itemService.getItem(itemId, userId);
            assertThat(actualItemDto)
                    .as("Проверка получения вещи по id ее владельцем, когда нет бронирований вещи")
                    .isNotNull()
                    .isEqualTo(expectedItemDto);
        }).doesNotThrowAnyException();

        verify(itemStorage, Mockito.times(1)).findById(itemId);
        verify(bookingStorage, Mockito.times(1)).findByItem_IdAndItem_User_IdAndStatusOrderByStartAsc(
                userId, itemId, Status.APPROVED);
        verify(commentStorage, Mockito.times(1)).findByItem_IdOrderByIdAsc(itemId);
    }

    @Test
    void getItem_WhenUserItemOwnerWithLastAndNextBookings_ThenReturnItemWithNotNullLastAndNextBooking() {
        long itemId = 1;
        long userId = 1;
        Item item = new Item(itemId, "name", "description", true, new User(userId),
                null);
        LocalDateTime start1 = LocalDateTime.now().plusDays(1);
        LocalDateTime end1 = LocalDateTime.now().plusDays(2);
        LocalDateTime start2 = LocalDateTime.now().minusDays(2);
        LocalDateTime end2 = LocalDateTime.now().minusDays(1);
        Booking booking1 = new Booking(1L, Status.APPROVED, item, new User(userId + 1), start2, end2);
        Booking booking2 = new Booking(2L, Status.APPROVED, item, new User(userId + 2), start1, end1);
        ItemWithBookingDto expectedItemDto = new ItemWithBookingDto(itemId, "name", "description",
                true, null, new SimpleBookingDto(1L, start2, end2, Status.APPROVED,
                userId + 1), new SimpleBookingDto(2L, start1, end1, Status.APPROVED, userId + 2),
                Collections.emptyList());
        when(userStorage.existsById(userId)).thenReturn(true);
        when(itemStorage.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingStorage.findByItem_IdAndItem_User_IdAndStatusOrderByStartAsc(itemId, userId, Status.APPROVED))
                .thenReturn(List.of(booking1, booking2));

        assertThatCode(() -> {
            ItemWithBookingDto actualItemDto = itemService.getItem(itemId, userId);
            assertThat(actualItemDto)
                    .as("Проверка получения вещи по id ее владельцем, когда нет бронирований вещи")
                    .isNotNull()
                    .isEqualTo(expectedItemDto);
        }).doesNotThrowAnyException();

        verify(itemStorage, Mockito.times(1)).findById(itemId);
        verify(bookingStorage, Mockito.times(1)).findByItem_IdAndItem_User_IdAndStatusOrderByStartAsc(
                userId, itemId, Status.APPROVED);
        verify(commentStorage, Mockito.times(1)).findByItem_IdOrderByIdAsc(itemId);
    }

    @Test
    void createComment_WhenUserHaveNoFinishedBookingsOfThisItem_ThenThrowsIllegalArgumentException() {
        CommentDto commentDto = new CommentDto(null, "text", null, null);
        long userId = 1;
        long itemId = 1;
        when(bookingStorage.existsByItem_IdAndBooker_IdAndStatusAndEndBefore(anyLong(), anyLong(), any(Status.class),
                any(LocalDateTime.class))).thenReturn(false);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .as("Проверка создания комментария, когда у пользователя нет завершенных бронирований на вещь" +
                        " с таким id")
                .isThrownBy(() -> itemService.createComment(commentDto, userId, itemId))
                .withMessage(Constants.USER_CANNOT_LEAVE_COMMENT_MESSAGE);
        verify(bookingStorage, Mockito.times(1)).existsByItem_IdAndBooker_IdAndStatusAndEndBefore(
                anyLong(), anyLong(), any(Status.class), any(LocalDateTime.class));
        verifyNoInteractions(commentStorage);
    }

    @Test
    void createComment_WhenUserAlreadyLeftCommentOnThisItem_ThenThrowsAlreadyExistException() {
        CommentDto commentDto = new CommentDto(null, "text", null, null);
        long userId = 1;
        long itemId = 1;
        when(bookingStorage.existsByItem_IdAndBooker_IdAndStatusAndEndBefore(anyLong(), anyLong(), any(Status.class),
                any(LocalDateTime.class))).thenReturn(true);
        when(commentStorage.existsByItem_IdAndAuthor_Id(itemId, userId)).thenReturn(true);

        assertThatExceptionOfType(AlreadyExistException.class)
                .as("Проверка создания комментария, когда у пользователя нет завершенных бронирований на вещь" +
                        " с таким id")
                .isThrownBy(() -> itemService.createComment(commentDto, userId, itemId))
                .withMessage(Constants.USER_CANNOT_LEAVE_COMMENT_TWICE_MESSAGE);
        verify(commentStorage, Mockito.times(1)).existsByItem_IdAndAuthor_Id(itemId, userId);
        verifyNoMoreInteractions(commentStorage);
    }

    @Test
    void createComment_WhenAllChecksHavePassed_ThenCommentCreated() {
        long userId = 1;
        long itemId = 1;
        LocalDateTime time = LocalDateTime.now();
        String text = "text";
        CommentDto commentDto = new CommentDto(11L, text, null, null);
        CommentDto expectedDto = new CommentDto(1L, text, "name", time);
        when(bookingStorage.existsByItem_IdAndBooker_IdAndStatusAndEndBefore(anyLong(), anyLong(), any(Status.class),
                any(LocalDateTime.class))).thenReturn(true);
        when(commentStorage.existsByItem_IdAndAuthor_Id(itemId, userId)).thenReturn(false);
        when(userStorage.getReferenceById(userId)).thenReturn(new User(userId));
        when(itemStorage.getReferenceById(itemId)).thenReturn(new Item(itemId));
        when(commentStorage.save(any(Comment.class))).thenReturn(new Comment(1L, "text", new Item(itemId, "name",
                "description", true, new User(2L, "name2", "mail2@mail.ru"),
                null), new User(userId, "name", "mail@mail.ru"), time));

        assertThatCode(() -> {
            CommentDto actualDto = itemService.createComment(commentDto, userId, itemId);
            assertThat(actualDto)
                    .as("Проверка сохранения комментария при корректных входных данных")
                    .isNotNull()
                    .isEqualTo(expectedDto);
        }).doesNotThrowAnyException();

        verify(commentStorage, Mockito.times(1)).save(commentArgumentCaptor.capture());
        assertThat(commentArgumentCaptor.getValue())
                .as("Проверка передаваемого аргумента в метод сохранения комментария")
                .hasFieldOrPropertyWithValue("id", 0L)
                .hasFieldOrPropertyWithValue("text", text)
                .hasFieldOrPropertyWithValue("item", new Item(itemId))
                .hasFieldOrPropertyWithValue("author", new User(userId));
        assertThat(commentArgumentCaptor.getValue().getCreated()).isNotNull();
    }
}