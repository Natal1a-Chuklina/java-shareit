package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithAnswersDto;
import ru.practicum.shareit.utils.Constants;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;

import static ru.practicum.shareit.utils.Constants.HEADER_WITH_USER_ID_NAME;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto createItemRequest(@RequestHeader(HEADER_WITH_USER_ID_NAME) @Positive long userId,
                                            @Valid @RequestBody CreateItemRequestDto createItemRequestDto) {
        log.info("Попытка создать запрос пользователем с id = {}", userId);
        return itemRequestService.createItemRequest(userId, createItemRequestDto);
    }

    @GetMapping
    public List<ItemRequestWithAnswersDto> getUsersRequests(@RequestHeader(HEADER_WITH_USER_ID_NAME)
                                                            @Positive long userId) {
        log.warn("Попытка получить все бронирования пользователя с id = {}", userId);
        return itemRequestService.getUserRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestWithAnswersDto getRequestById(@RequestHeader(HEADER_WITH_USER_ID_NAME) @Positive long userId,
                                                    @PathVariable @Positive long requestId) {
        log.info("Попытка получить бронирование с id = {} пользователем с id = {}", requestId, userId);
        return itemRequestService.getRequestById(userId, requestId);
    }

    @GetMapping("/all")
    public List<ItemRequestWithAnswersDto> getAllRequests(@RequestHeader(HEADER_WITH_USER_ID_NAME) @Positive long userId,
                                                          @RequestParam(defaultValue = Constants.DEFAULT_START_PAGE) @Min(0) int from,
                                                          @RequestParam(defaultValue = Constants.DEFAULT_PAGE_SIZE) @Positive int size) {
        log.info("Попытка получить {} запросов начиная с {}", size, from);
        return itemRequestService.getRequests(userId, from, size);
    }
}
