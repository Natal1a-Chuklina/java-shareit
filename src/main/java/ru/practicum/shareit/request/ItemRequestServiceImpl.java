package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemStorage;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswersDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.model.ItemRequestMapper;
import ru.practicum.shareit.user.UserStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestStorage itemRequestStorage;
    private final UserStorage userStorage;
    private final ItemStorage itemStorage;

    @Override
    public ItemRequestDto createItemRequest(long userId, CreateItemRequestDto createItemRequestDto) {
        checkUserExistence(userId,
                "Выполнена попытка создать запрос на вещь пользователем с несуществующим id = {}");

        User user = userStorage.getReferenceById(userId);
        ItemRequest createdRequest = itemRequestStorage.save(ItemRequestMapper.toItemRequest(createItemRequestDto,
                user));

        log.info("Создан запрос на вещь с id = {}, пользователем с id = {}", createdRequest.getId(), userId);
        return ItemRequestMapper.toItemRequestDto(createdRequest);
    }

    @Override
    public List<ItemRequestWithAnswersDto> getUserRequests(long userId) {
        checkUserExistence(userId, "Выполнена попытка получить все запросы пользователя с " +
                "несуществующим id = {}");

        List<ItemRequest> userRequests = itemRequestStorage.findByRequestor_IdOrderByCreatedDesc(userId);
        List<ItemRequestWithAnswersDto> userRequestsWithAnswers = new ArrayList<>();

        for (ItemRequest request : userRequests) {
            userRequestsWithAnswers.add(findAnswersForRequest(request));
        }

        log.info("Получен список запросов пользователя с id = {} длиной {}", userId, userRequestsWithAnswers.size());
        return userRequestsWithAnswers;
    }

    @Override
    public ItemRequestWithAnswersDto getRequestById(long userId, long requestId) {
        checkUserExistence(userId, "Выполнена попытка получить запрос на вещь пользователем с" +
                " несуществующим id = {}");

        Optional<ItemRequest> request = itemRequestStorage.findById(requestId);

        if (request.isEmpty()) {
            log.warn("Выполнена попытка получить запрос с несуществующим id = {} пользователем с id = {}",
                    requestId, userId);
            throw new NotFoundException(String.format(Constants.REQUEST_NOT_FOUND_MESSAGE, requestId));
        }

        return findAnswersForRequest(request.get());
    }

    @Override
    public List<ItemRequestWithAnswersDto> getRequests(long userId, int from, int size) {
        checkUserExistence(userId, "Выполнена попытка получить запросы пользователем с несуществующим id = {}");

        Sort sortByCreation = Sort.by(Sort.Direction.DESC, "created");
        Pageable page = PageRequest.of(from / size, size, sortByCreation);
        Page<ItemRequest> itemRequestsPage = itemRequestStorage.findByRequestor_IdNot(userId, page);

        List<ItemRequestWithAnswersDto> itemRequests = new ArrayList<>();
        itemRequestsPage.getContent().forEach(itemRequest -> itemRequests.add(findAnswersForRequest(itemRequest)));

        log.info("Получен список запросов длиной {}", itemRequests.size());
        return itemRequests;
    }

    private void checkUserExistence(long userId, String logMessage) {
        if (!userStorage.existsById(userId)) {
            log.warn(logMessage, userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        }
    }

    private ItemRequestWithAnswersDto findAnswersForRequest(ItemRequest request) {
        List<Item> itemsForRequest = itemStorage.findByItemRequest_Id(request.getId());
        log.info("Получен список вещей длиной {} созданных по запросу с id = {}", itemsForRequest.size(),
                request.getId());

        return ItemRequestMapper.toItemRequestWithAnswersDto(request, itemsForRequest);
    }
}
