package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.utils.Constants.HEADER_WITH_USER_ID_NAME;

@WebMvcTest(ItemController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemControllerITest {
    private final ObjectMapper objectMapper;
    private final MockMvc mockMvc;
    @MockBean
    private ItemService itemService;

    @SneakyThrows
    @Test
    void createItem_WhenAllParametersValid_ThenReturnOk() {
        ItemDto input = new ItemDto(0, "name", "description", true, 1L);
        ItemDto output = new ItemDto(1, "name", "description", true, 1L);

        when(itemService.createItem(1, input)).thenReturn(output);

        String actualOutput = mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input))
                        .header(HEADER_WITH_USER_ID_NAME, 1))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(actualOutput)
                .as("Проверка возвращаемого значения при создании вещи")
                .isNotNull()
                .isEqualTo(objectMapper.writeValueAsString(output));

        verify(itemService, Mockito.times(1)).createItem(1, input);
        Mockito.verifyNoMoreInteractions(itemService);
    }

    @SneakyThrows
    @Test
    void updateItem_WhenAllParametersValid_ThenReturnOk() {
        ItemDto input = new ItemDto(1, "new", "new_description", true, 1L);

        when(itemService.updateItem(1, input)).thenReturn(input);

        String actualOutput = mockMvc.perform(patch("/items/{itemId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input))
                        .header(HEADER_WITH_USER_ID_NAME, 1))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(actualOutput)
                .as("Проверка возвращаемого значения при обновлении вещи")
                .isNotNull()
                .isEqualTo(objectMapper.writeValueAsString(input));

        verify(itemService, Mockito.times(1)).updateItem(1, input);
        Mockito.verifyNoMoreInteractions(itemService);
    }

    @SneakyThrows
    @Test
    void getItem_WhenAllParametersValid_ThenReturnOk() {
        ItemWithBookingDto output = new ItemWithBookingDto(1, "name", "description", true,
                1L, null, null, new ArrayList<>());
        when(itemService.getItem(1, 1)).thenReturn(output);

        String actualOutput = mockMvc.perform(get("/items/{itemId}", 1)
                        .header(HEADER_WITH_USER_ID_NAME, 1))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(actualOutput)
                .as("Проверка возвращаемого значения при получении вещи по id")
                .isNotNull()
                .isEqualTo(objectMapper.writeValueAsString(output));
        verify(itemService, Mockito.times(1)).getItem(1, 1);
        Mockito.verifyNoMoreInteractions(itemService);
    }

    @SneakyThrows
    @Test
    void getUsersItems_WhenRequestParamsExist_ThenReturnOk() {
        ItemWithBookingDto itemWithBookingDto = new ItemWithBookingDto(1, "name", "description", true,
                1L, null, null, new ArrayList<>());
        when(itemService.getUsersItems(1, 1, 5)).thenReturn(List.of(itemWithBookingDto));

        String output = mockMvc.perform(get("/items")
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .param("from", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(output)
                .as("Проверка возвращаемого значения при получении вещей пользователя")
                .isNotNull()
                .isEqualTo(objectMapper.writeValueAsString(List.of(itemWithBookingDto)));
        verify(itemService, Mockito.times(1)).getUsersItems(1, 1, 5);
        Mockito.verifyNoMoreInteractions(itemService);
    }

    @SneakyThrows
    @Test
    void searchItems_WhenRequestParamsExist_ThenReturnOk() {
        ItemDto itemDto = new ItemDto(1, "new", "new_description", true, 1L);
        when(itemService.searchItems("search", 1, 5)).thenReturn(List.of(itemDto));

        String output = mockMvc.perform(get("/items/search")
                        .param("text", "search")
                        .param("from", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(output)
                .as("Проверка возвращаемого значения при получении вещей по поисковой строке")
                .isNotNull()
                .isEqualTo(objectMapper.writeValueAsString(List.of(itemDto)));
        verify(itemService, Mockito.times(1)).searchItems("search", 1, 5);
        Mockito.verifyNoMoreInteractions(itemService);
    }

    @SneakyThrows
    @Test
    void createComment_WhenAllParametersValid_ThenReturnOk() {
        CommentDto input = new CommentDto(0L, "text", null, null);
        when(itemService.createComment(input, 1, 1)).thenReturn(input);

        String actualOutput = mockMvc.perform(post("/items/{itemId}/comment", 1)
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(actualOutput)
                .as("Проверка возвращаемого значения при создании комментария")
                .isNotNull()
                .isEqualTo(objectMapper.writeValueAsString(input));
        verify(itemService, Mockito.times(1)).createComment(input, 1, 1);
        Mockito.verifyNoMoreInteractions(itemService);
    }
}