package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;

import static ru.practicum.shareit.utils.Constants.*;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createItemRequest(@RequestHeader(HEADER_WITH_USER_ID_NAME) @Positive long userId,
                                                    @Valid @RequestBody CreateItemRequestDto createItemRequestDto) {
        log.info("Попытка создать запрос пользователем с id = {}", userId);
        return itemRequestClient.createItemRequest(userId, createItemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getUsersRequests(@RequestHeader(HEADER_WITH_USER_ID_NAME) @Positive long userId) {
        log.warn("Попытка получить все бронирования пользователя с id = {}", userId);
        return itemRequestClient.getUsersRequest(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader(HEADER_WITH_USER_ID_NAME) @Positive long userId,
                                                 @PathVariable @Positive long requestId) {
        log.info("Попытка получить бронирование с id = {} пользователем с id = {}", requestId, userId);
        return itemRequestClient.getRequestById(userId, requestId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader(HEADER_WITH_USER_ID_NAME) @Positive long userId,
                                                 @RequestParam(defaultValue = DEFAULT_START_PAGE) @Min(0) int from,
                                                 @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) @Positive int size) {
        log.info("Попытка получить {} запросов начиная с {}", size, from);
        return itemRequestClient.getRequests(userId, from, size);
    }
}
