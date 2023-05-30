package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswersDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createItemRequest(long userId, CreateItemRequestDto createItemRequestDto);

    List<ItemRequestWithAnswersDto> getUserRequests(long userId);

    ItemRequestWithAnswersDto getRequestById(long userId, long requestId);

    List<ItemRequestWithAnswersDto> getRequests(long userId, int from, int size);
}
