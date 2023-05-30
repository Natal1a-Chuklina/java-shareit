package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.CreateItemRequestDto;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.utils.Constants.HEADER_WITH_USER_ID_NAME;

@WebMvcTest(ItemRequestController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestControllerITest {
    private final ObjectMapper objectMapper;
    private final MockMvc mockMvc;
    @MockBean
    private ItemRequestClient itemRequestClient;

    @SneakyThrows
    @Test
    void createItemRequest_WhenNoRequestHeader_ThenReturnBadRequest() {
        CreateItemRequestDto input = new CreateItemRequestDto("description");

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemRequestClient);
    }

    @SneakyThrows
    @Test
    void createItemRequest_WhenNotValidRequestHeader_ThenReturnBadRequest() {
        CreateItemRequestDto input = new CreateItemRequestDto("description");

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input))
                        .header(HEADER_WITH_USER_ID_NAME, -1))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input))
                        .header(HEADER_WITH_USER_ID_NAME, 0))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemRequestClient);
    }

    @SneakyThrows
    @Test
    void createItemRequest_WhenNotValidRequestBody_ThenReturnBadRequest() {
        CreateItemRequestDto nullDescriptionDto = new CreateItemRequestDto(null);
        CreateItemRequestDto emptyDescriptionDto = new CreateItemRequestDto("  ");


        mockMvc.perform(post("/requests")
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nullDescriptionDto)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/requests")
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyDescriptionDto)))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemRequestClient);
    }

    @SneakyThrows
    @Test
    void createItemRequest_WhenAllParametersValid_ThenReturnOk() {
        CreateItemRequestDto input = new CreateItemRequestDto("description");

        mockMvc.perform(post("/requests")
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk());

        verify(itemRequestClient, Mockito.times(1)).createItemRequest(1, input);
        Mockito.verifyNoMoreInteractions(itemRequestClient);
    }

    @SneakyThrows
    @Test
    void getUsersRequests_WhenNoRequestHeader_ThenReturnBadRequest() {
        mockMvc.perform(get("/requests"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemRequestClient);
    }

    @SneakyThrows
    @Test
    void getUsersRequests_WhenNotValidRequestHeader_ThenReturnBadRequest() {
        mockMvc.perform(get("/requests")
                        .header(HEADER_WITH_USER_ID_NAME, -1))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/requests")
                        .header(HEADER_WITH_USER_ID_NAME, 0))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemRequestClient);
    }

    @SneakyThrows
    @Test
    void getUsersRequests_WhenRequestHeaderIsValid_ThenReturnOk() {
        mockMvc.perform(get("/requests")
                        .header(HEADER_WITH_USER_ID_NAME, 1))
                .andExpect(status().isOk());

        verify(itemRequestClient, Mockito.times(1)).getUsersRequest(1);
        Mockito.verifyNoMoreInteractions(itemRequestClient);
    }

    @SneakyThrows
    @Test
    void getRequestById_WhenNoRequestHeader_ThenReturnBadRequest() {
        mockMvc.perform(get("/requests/{requestId}", 1))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemRequestClient);
    }

    @SneakyThrows
    @Test
    void getRequestById_WhenNotValidRequestHeader_ThenReturnBadRequest() {
        mockMvc.perform(get("/requests/{requestId}", 1)
                        .header(HEADER_WITH_USER_ID_NAME, -1))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/requests/{requestId}", 1)
                        .header(HEADER_WITH_USER_ID_NAME, 0))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemRequestClient);
    }

    @SneakyThrows
    @Test
    void getRequestById_WhenNotValidPathVariable_ThenReturnBadRequest() {
        mockMvc.perform(get("/requests/{requestId}", -1)
                        .header(HEADER_WITH_USER_ID_NAME, 1))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/requests/{requestId}", 0)
                        .header(HEADER_WITH_USER_ID_NAME, 1))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemRequestClient);
    }

    @SneakyThrows
    @Test
    void getRequestById_WhenAllParametersValid_ThenReturnOk() {
        mockMvc.perform(get("/requests/{requestId}", 1)
                        .header(HEADER_WITH_USER_ID_NAME, 1))
                .andExpect(status().isOk());

        verify(itemRequestClient, Mockito.times(1)).getRequestById(1, 1);
        Mockito.verifyNoMoreInteractions(itemRequestClient);
    }

    @SneakyThrows
    @Test
    void getAllRequests_WhenNoRequestHeader_ThenReturnBadRequest() {
        mockMvc.perform(get("/requests/all")
                        .param("from", "1")
                        .param("size", "5"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemRequestClient);
    }

    @SneakyThrows
    @Test
    void getAllRequests_WhenNotValidRequestHeader_ThenReturnBadRequest() {
        mockMvc.perform(get("/requests/all")
                        .header(HEADER_WITH_USER_ID_NAME, -1)
                        .param("from", "1")
                        .param("size", "5"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/requests/all")
                        .header(HEADER_WITH_USER_ID_NAME, 0)
                        .param("from", "1")
                        .param("size", "5"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemRequestClient);
    }

    @SneakyThrows
    @Test
    void getAllRequests_WhenNotValidRequestParams_ThenReturnBadRequest() {
        mockMvc.perform(get("/requests/all")
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .param("from", "-1")
                        .param("size", "5"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/requests/all")
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .param("from", "1")
                        .param("size", "-5"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/requests/all")
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(itemRequestClient);
    }

    @SneakyThrows
    @Test
    void getAllRequests_WhenNoRequestParams_ThenReturnOk() {
        mockMvc.perform(get("/requests/all")
                        .header(HEADER_WITH_USER_ID_NAME, 1))
                .andExpect(status().isOk());

        verify(itemRequestClient, Mockito.times(1)).getRequests(1, 0, 20);
        Mockito.verifyNoMoreInteractions(itemRequestClient);
    }

    @SneakyThrows
    @Test
    void getAllRequests_WhenRequestParamsExist_ThenReturnOk() {
        mockMvc.perform(get("/requests/all")
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .param("from", "1")
                        .param("size", "5"))
                .andExpect(status().isOk());

        verify(itemRequestClient, Mockito.times(1)).getRequests(1, 1, 5);
        Mockito.verifyNoMoreInteractions(itemRequestClient);
    }
}