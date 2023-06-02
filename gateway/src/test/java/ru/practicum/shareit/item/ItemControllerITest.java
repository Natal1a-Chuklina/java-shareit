package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;
import java.util.stream.Stream;

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
    private ItemClient itemClient;

    @SneakyThrows
    @Test
    void createItem_WhenNoRequestHeader_ThenReturnBadRequest() {
        ItemDto input = new ItemDto(0, "name", "description", true, null);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemClient);
    }

    @SneakyThrows
    @Test
    void createItem_WhenNotValidRequestHeader_ThenReturnBadRequest() {
        ItemDto input = new ItemDto(0, "name", "description", true, null);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input))
                        .header(HEADER_WITH_USER_ID_NAME, -1))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input))
                        .header(HEADER_WITH_USER_ID_NAME, 0))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemClient);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("wrongItemDtosStream")
    void createItem_WhenNotValidRequestBody_ThenReturnBadRequest(ItemDto input) {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input))
                        .header(HEADER_WITH_USER_ID_NAME, 1))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemClient);
    }

    static Stream<ItemDto> wrongItemDtosStream() {
        ItemDto nullNameDto = new ItemDto(0, null, "description", true, null);
        ItemDto emptyNameDto = new ItemDto(0, "  ", "description", true, null);
        ItemDto nullDescriptionDto = new ItemDto(0, "name", null, true, null);
        ItemDto emptyDescriptionDto = new ItemDto(0, "name", "  ", true, null);
        ItemDto nullAvailableDto = new ItemDto(0, "name", "description", null, null);
        ItemDto nullDto = new ItemDto(0, null, null, null, null);

        return Stream.of(nullNameDto, emptyNameDto, nullDescriptionDto, emptyDescriptionDto, nullAvailableDto,
                nullDto);
    }

    @SneakyThrows
    @Test
    void createItem_WhenAllParametersValid_ThenReturnOk() {
        ItemDto input = new ItemDto(0, "name", "description", true, 1L);
        ItemDto output = new ItemDto(1, "name", "description", true, 1L);
        ResponseEntity<Object> response = new ResponseEntity<>(output, HttpStatus.OK);
        when(itemClient.createItem(1, input)).thenReturn(response);

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

        verify(itemClient, Mockito.times(1)).createItem(1, input);
        Mockito.verifyNoMoreInteractions(itemClient);
    }

    @SneakyThrows
    @Test
    void updateItem_WhenNoRequestHeader_ThenReturnBadRequest() {
        ItemDto input = new ItemDto(0, "name", "description", true, null);

        mockMvc.perform(patch("/items/{itemId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemClient);
    }

    @SneakyThrows
    @Test
    void updateItem_WhenNotValidRequestHeader_ThenReturnBadRequest() {
        ItemDto input = new ItemDto(0, "name", "description", true, null);

        mockMvc.perform(patch("/items/{itemId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input))
                        .header(HEADER_WITH_USER_ID_NAME, -1))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/items/{itemId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input))
                        .header(HEADER_WITH_USER_ID_NAME, 0))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemClient);
    }

    @SneakyThrows
    @Test
    void updateItem_WhenNotValidPathVariable_ThenReturnBadRequest() {
        ItemDto input = new ItemDto(0, "name", "description", true, null);

        mockMvc.perform(patch("/items/{itemId}", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input))
                        .header(HEADER_WITH_USER_ID_NAME, 1))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/items/{itemId}", -1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input))
                        .header(HEADER_WITH_USER_ID_NAME, 1))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemClient);
    }

    @SneakyThrows
    @Test
    void updateItem_WhenAllParametersValid_ThenReturnOk() {
        ItemDto input = new ItemDto(1, "new", "new_description", true, 1L);
        ResponseEntity<Object> response = new ResponseEntity<>(input, HttpStatus.OK);
        when(itemClient.updateItem(1, input, 1)).thenReturn(response);

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

        verify(itemClient, Mockito.times(1)).updateItem(1, input, 1);
        Mockito.verifyNoMoreInteractions(itemClient);
    }

    @SneakyThrows
    @Test
    void getItem_WhenNoRequestHeader_ThenReturnBadRequest() {
        mockMvc.perform(get("/items/{itemId}", 1))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemClient);
    }

    @SneakyThrows
    @Test
    void getItem_WhenNotValidRequestHeader_ThenReturnBadRequest() {
        mockMvc.perform(get("/items/{itemId}", 1)
                        .header(HEADER_WITH_USER_ID_NAME, -1))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/items/{itemId}", 1)
                        .header(HEADER_WITH_USER_ID_NAME, 0))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemClient);
    }

    @SneakyThrows
    @Test
    void getItem_WhenNotValidPathVariable_ThenReturnBadRequest() {
        mockMvc.perform(get("/items/{itemId}", 0)
                        .header(HEADER_WITH_USER_ID_NAME, 1))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/items/{itemId}", -1)
                        .header(HEADER_WITH_USER_ID_NAME, 1))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemClient);
    }

    @SneakyThrows
    @Test
    void getItem_WhenAllParametersValid_ThenReturnOk() {
        mockMvc.perform(get("/items/{itemId}", 1)
                        .header(HEADER_WITH_USER_ID_NAME, 1))
                .andExpect(status().isOk());

        verify(itemClient, Mockito.times(1)).getItem(1, 1);
        Mockito.verifyNoMoreInteractions(itemClient);
    }

    @SneakyThrows
    @Test
    void getUsersItems_WhenNoRequestHeader_ThenReturnBadRequest() {
        mockMvc.perform(get("/items")
                        .param("from", "0")
                        .param("size", "5"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemClient);
    }

    @SneakyThrows
    @Test
    void getUsersItems_WhenNotValidRequestHeader_ThenReturnBadRequest() {
        mockMvc.perform(get("/items")
                        .header(HEADER_WITH_USER_ID_NAME, -1)
                        .param("from", "0")
                        .param("size", "5"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/items")
                        .header(HEADER_WITH_USER_ID_NAME, 0)
                        .param("from", "0")
                        .param("size", "5"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemClient);
    }

    @SneakyThrows
    @Test
    void getUsersItems_WhenNotValidRequestParams_ThenReturnBadRequest() {
        mockMvc.perform(get("/items")
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .param("from", "-1")
                        .param("size", "5"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/items")
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .param("from", "1")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/items")
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .param("from", "1")
                        .param("size", "-5"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemClient);
    }

    @SneakyThrows
    @Test
    void getUsersItems_WhenNoRequestParams_ThenReturnOk() {
        mockMvc.perform(get("/items")
                        .header(HEADER_WITH_USER_ID_NAME, 1))
                .andExpect(status().isOk());

        verify(itemClient, Mockito.times(1)).getUsersItems(1, 0, 20);
        Mockito.verifyNoMoreInteractions(itemClient);
    }

    @SneakyThrows
    @Test
    void getUsersItems_WhenRequestParamsExist_ThenReturnOk() {
        mockMvc.perform(get("/items")
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .param("from", "1")
                        .param("size", "5"))
                .andExpect(status().isOk());

        verify(itemClient, Mockito.times(1)).getUsersItems(1, 1, 5);
        Mockito.verifyNoMoreInteractions(itemClient);
    }

    @SneakyThrows
    @Test
    void searchItems_WhenNoRequestHeader_ThenReturnBadRequest() {
        mockMvc.perform(get("/items/search")
                        .param("text", "search")
                        .param("from", "0")
                        .param("size", "5"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemClient);
    }

    @SneakyThrows
    @Test
    void searchItems_WhenNotValidRequestHeader_ThenReturnBadRequest() {
        mockMvc.perform(get("/items/search")
                        .header(HEADER_WITH_USER_ID_NAME, -1)
                        .param("text", "search")
                        .param("from", "0")
                        .param("size", "5"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/items/search")
                        .header(HEADER_WITH_USER_ID_NAME, 0)
                        .param("text", "search")
                        .param("from", "0")
                        .param("size", "5"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemClient);
    }

    @SneakyThrows
    @Test
    void searchItems_WhenNotValidRequestParams_ThenReturnBadRequest() {
        mockMvc.perform(get("/items/search")
                        .param("text", "search")
                        .param("from", "-1")
                        .param("size", "5"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/items")
                        .param("text", "search")
                        .param("from", "1")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/items")
                        .param("text", "search")
                        .param("from", "1")
                        .param("size", "-5"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemClient);
    }

    @SneakyThrows
    @Test
    void searchItems_WhenNoRequestParams_ThenReturnOk() {
        ItemDto itemDto = new ItemDto(1, "new", "new_description", true, 1L);
        ResponseEntity<Object> response = new ResponseEntity<>(List.of(itemDto), HttpStatus.OK);
        when(itemClient.searchItems(1, "search", 0, 20)).thenReturn(response);

        String output = mockMvc.perform(get("/items/search")
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .param("text", "search"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();


        assertThat(output)
                .as("Проверка возвращаемого значения при получении вещей по поисковой строке")
                .isNotNull()
                .isEqualTo(objectMapper.writeValueAsString(List.of(itemDto)));
        verify(itemClient, Mockito.times(1)).searchItems(1, "search", 0, 20);
        Mockito.verifyNoMoreInteractions(itemClient);
    }

    @SneakyThrows
    @Test
    void searchItems_WhenRequestParamsExist_ThenReturnOk() {
        mockMvc.perform(get("/items/search")
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .param("text", "search")
                        .param("from", "1")
                        .param("size", "5"))
                .andExpect(status().isOk());

        verify(itemClient, Mockito.times(1)).searchItems(1, "search", 1, 5);
        Mockito.verifyNoMoreInteractions(itemClient);
    }

    @SneakyThrows
    @Test
    void createComment_WhenNoRequestHeader_ThenReturnBadRequest() {
        CommentDto input = new CommentDto(0L, "text", null, null);

        mockMvc.perform(post("/items/{itemId}/comment", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemClient);
    }

    @SneakyThrows
    @Test
    void createComment_WhenNotValidRequestHeader_ThenReturnBadRequest() {
        CommentDto input = new CommentDto(0L, "text", null, null);

        mockMvc.perform(post("/items/{itemId}/comment", 1)
                        .header(HEADER_WITH_USER_ID_NAME, -1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/items/{itemId}/comment", 1)
                        .header(HEADER_WITH_USER_ID_NAME, 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemClient);
    }

    @SneakyThrows
    @Test
    void createComment_WhenNotValidPathVariable_ThenReturnBadRequest() {
        CommentDto input = new CommentDto(0L, "text", null, null);

        mockMvc.perform(post("/items/{itemId}/comment", -1)
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/items/{itemId}/comment", 0)
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemClient);
    }

    @SneakyThrows
    @Test
    void createComment_WhenNotValidRequestBody_ThenReturnBadRequest() {
        CommentDto nullTextDto = new CommentDto(0L, null, null, null);
        CommentDto emptyTextDto = new CommentDto(0L, "  ", null, null);

        mockMvc.perform(post("/items/{itemId}/comment", 1)
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nullTextDto)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/items/{itemId}/comment", 1)
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyTextDto)))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemClient);
    }

    @SneakyThrows
    @Test
    void createComment_WhenAllParametersValid_ThenReturnOk() {
        CommentDto input = new CommentDto(0L, "text", null, null);
        ResponseEntity<Object> response = new ResponseEntity<>(input, HttpStatus.OK);
        when(itemClient.createComment(input, 1, 1)).thenReturn(response);

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
        verify(itemClient, Mockito.times(1)).createComment(input, 1, 1);
        Mockito.verifyNoMoreInteractions(itemClient);
    }
}