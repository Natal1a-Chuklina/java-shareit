package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;

import static ru.practicum.shareit.utils.Constants.*;


@RestController
@RequestMapping("/items")
@Validated
@Slf4j
@RequiredArgsConstructor
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader(HEADER_WITH_USER_ID_NAME) @Positive long userId,
                                             @Valid @RequestBody ItemDto itemDto) {
        log.info("Попытка создать вещь пользователем с id = {}", userId);
        return itemClient.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(HEADER_WITH_USER_ID_NAME) @Positive long userId,
                                             @RequestBody ItemDto itemDto, @PathVariable @Positive long itemId) {
        log.info("Попытка обновить вещь с id = {} пользователем с id = {}", itemId, userId);
        return itemClient.updateItem(userId, itemDto, itemId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@RequestHeader(HEADER_WITH_USER_ID_NAME) @Positive long userId,
                                          @PathVariable @Positive long itemId) {
        log.info("Попытка получить вещь с id = {} пользователем с id = {}", itemId, userId);
        return itemClient.getItem(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getUsersItems(@RequestHeader(HEADER_WITH_USER_ID_NAME) @Positive long userId,
                                                @RequestParam(defaultValue = DEFAULT_START_PAGE) @Min(0) int from,
                                                @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) @Positive int size) {
        log.info("Попытка получить {} вещей пользователя с id = {} начиная с {} вещи", size, userId, from);
        return itemClient.getUsersItems(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestHeader(HEADER_WITH_USER_ID_NAME) @Positive long userId,
                                              @RequestParam String text,
                                              @RequestParam(defaultValue = DEFAULT_START_PAGE) @Min(0) int from,
                                              @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) @Positive int size) {
        log.info("Попытка найти {} вещей начиная с {} по поисковой строке: {}", size, from, text);
        return itemClient.searchItems(userId, text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader(HEADER_WITH_USER_ID_NAME) @Positive long userId,
                                                @PathVariable @Positive long itemId,
                                                @Valid @RequestBody CommentDto commentDto) {
        log.info("Попытка оставить отзыв на вещь с id = {} пользователем с id = {}", itemId, userId);
        return itemClient.createComment(commentDto, userId, itemId);
    }
}
