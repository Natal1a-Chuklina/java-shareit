package ru.practicum.shareit.request.model;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemMapper;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswersDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

public class ItemRequestMapper {
    private ItemRequestMapper() {
    }

    public static ItemRequest toItemRequest(CreateItemRequestDto createItemRequestDto, User user) {
        return new ItemRequest(
                createItemRequestDto.getDescription(),
                user,
                LocalDateTime.now()
        );
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return new ItemRequestDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getCreated()
        );
    }

    public static ItemRequestWithAnswersDto toItemRequestWithAnswersDto(ItemRequest itemRequest, List<Item> items) {
        return new ItemRequestWithAnswersDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getCreated(),
                ItemMapper.toItemDto(items)
        );
    }
}
