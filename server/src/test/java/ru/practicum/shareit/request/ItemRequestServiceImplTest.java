package ru.practicum.shareit.request;

import org.assertj.core.data.Index;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemStorage;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswersDto;
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
class ItemRequestServiceImplTest {
    @Mock
    private ItemRequestStorage itemRequestStorage;
    @Mock
    private UserStorage userStorage;
    @Mock
    private ItemStorage itemStorage;
    @InjectMocks
    private ItemRequestServiceImpl requestService;
    @Captor
    private ArgumentCaptor<ItemRequest> requestArgumentCaptor;

    @Test
    void createItemRequest_WhenUserDoesNotExist_ThenThrowsNotFoundException() {
        CreateItemRequestDto requestDto = new CreateItemRequestDto("description");
        long userId = 1;
        when(userStorage.existsById(userId)).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class)
                .as("Проверка создания запроса на вещь пользователем, id которого нет в базе")
                .isThrownBy(() -> requestService.createItemRequest(userId, requestDto))
                .withMessage(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));

        verify(userStorage, Mockito.times(1)).existsById(userId);
        verifyNoInteractions(itemRequestStorage);
    }

    @Test
    void createItemRequest_WhenUserExists_ThenRequestCreated() {
        CreateItemRequestDto requestDto = new CreateItemRequestDto("description");
        long userId = 1;
        LocalDateTime time = LocalDateTime.now();
        ItemRequestDto expectedDto = new ItemRequestDto(1L, "description", time);
        when(userStorage.existsById(userId)).thenReturn(true);
        when(userStorage.getReferenceById(userId)).thenReturn(new User(userId));
        when(itemRequestStorage.save(any(ItemRequest.class))).thenReturn(new ItemRequest(1L,
                "description", new User(userId), time));

        assertThatCode(() -> {
            ItemRequestDto actualDto = requestService.createItemRequest(userId, requestDto);

            assertThat(actualDto)
                    .as("Проверка создания запроса на вещь при корректных входных данных")
                    .isNotNull()
                    .isEqualTo(expectedDto);
        }).doesNotThrowAnyException();

        verify(userStorage, Mockito.times(1)).getReferenceById(userId);
        verify(itemRequestStorage, Mockito.times(1)).save(requestArgumentCaptor.capture());
        assertThat(requestArgumentCaptor.getValue())
                .as("Проверка передаваемого аргумента в метод сохранения запроса на вещь")
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", null)
                .hasFieldOrPropertyWithValue("description", requestDto.getDescription())
                .hasFieldOrPropertyWithValue("requestor", new User(userId));
        assertThat(requestArgumentCaptor.getValue().getCreated()).isNotNull();
        verifyNoMoreInteractions(itemRequestStorage);
    }

    @Test
    void getUsersRequests_WhenUserDoesNotExist_ThenThrowsNotFoundException() {
        long userId = 1;
        when(userStorage.existsById(userId)).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class)
                .as("Проверка создания запроса на вещь пользователем, id которого нет в базе")
                .isThrownBy(() -> requestService.getUserRequests(userId))
                .withMessage(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));

        verify(userStorage, Mockito.times(1)).existsById(userId);
        verifyNoInteractions(itemRequestStorage);
    }

    @Test
    void getUsersRequests_WhenUserDoesNotHaveRequests_ThenReturnEmptyList() {
        long userId = 1;
        when(userStorage.existsById(userId)).thenReturn(true);
        when(itemRequestStorage.findByRequestor_IdOrderByCreatedDesc(userId)).thenReturn(Collections.emptyList());

        assertThatCode(() -> {
            List<ItemRequestWithAnswersDto> items = requestService.getUserRequests(userId);
            assertThat(items)
                    .as("Проверка получения всех запросов на вещи пользователя, когда запросов нет в базе")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();

        verify(itemRequestStorage, Mockito.times(1)).findByRequestor_IdOrderByCreatedDesc(userId);
        verifyNoInteractions(itemStorage);
    }

    @Test
    void getUsersRequests_WhenUserHasRequests_ThenReturnNotEmptyList() {
        long userId = 1;
        when(userStorage.existsById(userId)).thenReturn(true);
        LocalDateTime created1 = LocalDateTime.now().minusDays(1);
        LocalDateTime created2 = LocalDateTime.now().minusDays(2);
        ItemRequest itemRequest1 = new ItemRequest(1L, "description1", new User(userId), created1);
        ItemRequest itemRequest2 = new ItemRequest(2L, "description2", new User(userId), created2);
        when(itemRequestStorage.findByRequestor_IdOrderByCreatedDesc(userId)).thenReturn(List.of(itemRequest1,
                itemRequest2));
        Item item1 = new Item(1L, "name1", "description1", true, new User(userId + 1),
                itemRequest1);
        Item item2 = new Item(2L, "name2", "description2", true, new User(userId + 2),
                itemRequest1);
        when(itemStorage.findByItemRequest_Id(itemRequest1.getId())).thenReturn(List.of(item1, item2));
        ItemRequestWithAnswersDto expectedRequestDto1 = new ItemRequestWithAnswersDto(1L, "description1",
                created1, List.of(new ItemDto(1, "name1", "description1", true, 1L),
                new ItemDto(2, "name2", "description2", true, 1L)));
        ItemRequestWithAnswersDto expectedRequestDto2 = new ItemRequestWithAnswersDto(2L, "description2",
                created2, Collections.emptyList());

        assertThatCode(() -> {
            List<ItemRequestWithAnswersDto> answer = requestService.getUserRequests(userId);

            assertThat(answer)
                    .as("Проверка получения непустого списка бронирований пользователя с ответами")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(expectedRequestDto1, Index.atIndex(0))
                    .contains(expectedRequestDto2, Index.atIndex(1));
        }).doesNotThrowAnyException();

        verify(itemRequestStorage, Mockito.times(1)).findByRequestor_IdOrderByCreatedDesc(userId);
        verify(itemStorage, Mockito.times(1)).findByItemRequest_Id(itemRequest1.getId());
        verify(itemStorage, Mockito.times(1)).findByItemRequest_Id(itemRequest2.getId());
        verifyNoMoreInteractions(itemRequestStorage);
        verifyNoMoreInteractions(itemStorage);
    }

    @Test
    void getRequestById_WhenUserDoesNotExist_ThenThrowsNotFoundException() {
        long userId = 1;
        long requestId = 1;
        when(userStorage.existsById(userId)).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class)
                .as("Проверка создания запроса на вещь пользователем, id которого нет в базе")
                .isThrownBy(() -> requestService.getRequestById(userId, requestId))
                .withMessage(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));

        verify(userStorage, Mockito.times(1)).existsById(userId);
        verifyNoInteractions(itemRequestStorage);
    }

    @Test
    void getRequestById_WhenRequestDoesNotExist_ThenThrowsNotFoundException() {
        long userId = 1;
        long requestId = 1;
        when(userStorage.existsById(userId)).thenReturn(true);
        when(itemRequestStorage.findById(requestId)).thenReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .as("Проверка создания запроса на вещь пользователем, id которого нет в базе")
                .isThrownBy(() -> requestService.getRequestById(userId, requestId))
                .withMessage(String.format(Constants.REQUEST_NOT_FOUND_MESSAGE, requestId));

        verify(itemRequestStorage, Mockito.times(1)).findById(userId);
        verifyNoMoreInteractions(itemRequestStorage);
    }

    @Test
    void getRequestById_WhenRequestAnswersNotExist_ThenReturnDtoWithEmptyList() {
        long userId = 1;
        long requestId = 1;
        LocalDateTime created = LocalDateTime.now().minusDays(1);
        ItemRequest itemRequest = new ItemRequest(1L, "description", new User(userId), created);
        when(userStorage.existsById(userId)).thenReturn(true);
        when(itemRequestStorage.findById(userId)).thenReturn(Optional.of(itemRequest));
        when(itemStorage.findByItemRequest_Id(itemRequest.getId())).thenReturn(Collections.emptyList());
        ItemRequestWithAnswersDto expectedRequestDto = new ItemRequestWithAnswersDto(1L, "description",
                created, Collections.emptyList());

        assertThatCode(() -> {
            ItemRequestWithAnswersDto actualDto = requestService.getRequestById(userId, requestId);
            assertThat(actualDto)
                    .as("Проверка получения запроса на вещь по id, когда у запроса нет ответов")
                    .isNotNull()
                    .isEqualTo(expectedRequestDto);
        }).doesNotThrowAnyException();

        verify(itemRequestStorage, Mockito.times(1)).findById(requestId);
        verify(itemStorage, Mockito.times(1)).findByItemRequest_Id(requestId);
        verifyNoMoreInteractions(itemRequestStorage);
        verifyNoMoreInteractions(itemStorage);
    }

    @Test
    void getRequestById_WhenRequestAnswersExist_ThenReturnDtoWithNotEmptyList() {
        long userId = 1;
        long requestId = 1;
        LocalDateTime created = LocalDateTime.now().minusDays(1);
        ItemRequest itemRequest = new ItemRequest(1L, "description", new User(userId), created);
        Item item1 = new Item(1L, "name1", "description1", true, new User(userId + 1),
                itemRequest);
        Item item2 = new Item(2L, "name2", "description2", true, new User(userId + 2),
                itemRequest);
        when(userStorage.existsById(userId)).thenReturn(true);
        when(itemRequestStorage.findById(userId)).thenReturn(Optional.of(itemRequest));
        when(itemStorage.findByItemRequest_Id(itemRequest.getId())).thenReturn(List.of(item1, item2));
        ItemRequestWithAnswersDto expectedRequestDto = new ItemRequestWithAnswersDto(1L, "description",
                created, List.of(new ItemDto(1, "name1", "description1", true, 1L),
                new ItemDto(2, "name2", "description2", true, 1L)));

        assertThatCode(() -> {
            ItemRequestWithAnswersDto actualDto = requestService.getRequestById(userId, requestId);
            assertThat(actualDto)
                    .as("Проверка получения запроса на вещь по id, когда у запроса есть ответы")
                    .isNotNull()
                    .isEqualTo(expectedRequestDto);
        }).doesNotThrowAnyException();

        verify(itemRequestStorage, Mockito.times(1)).findById(requestId);
        verify(itemStorage, Mockito.times(1)).findByItemRequest_Id(requestId);
        verifyNoMoreInteractions(itemRequestStorage);
        verifyNoMoreInteractions(itemStorage);
    }

    @Test
    void getRequests_WhenUserDoesNotExist_ThenThrowsNotFoundException() {
        long userId = 1;
        int from = 0;
        int size = 5;
        when(userStorage.existsById(userId)).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class)
                .as("Проверка получения запросов пользователем, id которого нет в базе")
                .isThrownBy(() -> requestService.getRequests(userId, from, size))
                .withMessage(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
    }
}