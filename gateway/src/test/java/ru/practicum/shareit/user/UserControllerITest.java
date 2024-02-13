package ru.practicum.shareit.user;

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
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.utils.Constants.NOT_EMPTY_EMAIL_MESSAGE;
import static ru.practicum.shareit.utils.Constants.NOT_EMPTY_USER_NAME_MESSAGE;

@WebMvcTest(UserController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserControllerITest {
    private final ObjectMapper objectMapper;
    private final MockMvc mockMvc;
    @MockBean
    private UserClient userClient;

    @SneakyThrows
    @Test
    void createUser_WhenEmailNotValid_ThenReturnBadRequest() {
        UserDto inputDto = new UserDto(0, "name", "wrong_mail");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(userClient);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("wrongNameUserDtosStream")
    void createUser_WhenNameIsNotValid_ThenReturnBadRequest(UserDto inputDto) {
        assertThat(mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8))
                .contains(NOT_EMPTY_USER_NAME_MESSAGE);

        Mockito.verifyNoInteractions(userClient);
    }

    static Stream<UserDto> wrongNameUserDtosStream() {
        UserDto inputDto1 = new UserDto(0, " ", "email@mail.ru");
        UserDto inputDto2 = new UserDto(0, null, "email@mail.ru");
        UserDto inputDto3 = new UserDto(0, null, null);
        UserDto inputDto4 = new UserDto(0, "  ", "");

        return Stream.of(inputDto1, inputDto2, inputDto3, inputDto4);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("wrongEmailUserDtosStream")
    void createUser_WhenEmailIsNotValid_ThenReturnBadRequest(UserDto inputDto) {
        assertThat(mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8))
                .contains(NOT_EMPTY_EMAIL_MESSAGE);

        Mockito.verifyNoInteractions(userClient);
    }

    static Stream<UserDto> wrongEmailUserDtosStream() {
        UserDto inputDto1 = new UserDto(0, "name", null);
        UserDto inputDto2 = new UserDto(0, "name", "");

        return Stream.of(inputDto1, inputDto2);
    }

    @SneakyThrows
    @Test
    void createUser_WhenDtoIsValid_ThenReturnOk() {
        UserDto inputDto = new UserDto(0, "name", "mail@mail.ru");
        UserDto outputDto = new UserDto(1, "name", "mail@mail.ru");
        ResponseEntity<Object> response = new ResponseEntity<>(outputDto, HttpStatus.OK);

        when(userClient.createUser(inputDto)).thenReturn(response);

        String actualOutput = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(actualOutput)
                .as("Проверка возвращаемого значения при создании пользователя")
                .isNotNull()
                .isEqualTo(objectMapper.writeValueAsString(outputDto));
        verify(userClient, Mockito.times(1)).createUser(inputDto);
        Mockito.verifyNoMoreInteractions(userClient);
    }

    @SneakyThrows
    @Test
    void getAll_WhenUsersDoNotExist_ThenReturnOk() {
        ArrayList<UserDto> emptyList = new ArrayList<>();
        ResponseEntity<Object> response = new ResponseEntity<>(emptyList, HttpStatus.OK);
        when(userClient.getAllUsers()).thenReturn(response);

        String output = mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(output)
                .as("Проверка вывода пустого списка при отсутствии пользователей")
                .isNotNull()
                .isEqualTo(objectMapper.writeValueAsString(emptyList));
        verify(userClient, Mockito.times(1)).getAllUsers();
        Mockito.verifyNoMoreInteractions(userClient);
    }

    @SneakyThrows
    @Test
    void getAll_WhenUsersExist_ThenReturnOk() {
        List<UserDto> users = List.of(new UserDto(1, "name1", "mail1@mail.ru"),
                new UserDto(2, "name2", "mail2@mail.ru"));
        ResponseEntity<Object> response = new ResponseEntity<>(users, HttpStatus.OK);
        when(userClient.getAllUsers()).thenReturn(response);

        String output = mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(output)
                .as("Проверка вывода непустого списка")
                .isNotNull()
                .isEqualTo(objectMapper.writeValueAsString(users));
        verify(userClient, Mockito.times(1)).getAllUsers();
        Mockito.verifyNoMoreInteractions(userClient);
    }

    @SneakyThrows
    @Test
    void getUser_WhenIdIsNotValid_ThenReturnBadRequest() {
        mockMvc.perform(get("/users/{userId}", -1))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/users/{userId}", 0))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(userClient);
    }

    @SneakyThrows
    @Test
    void getUser_WhenIdIsValid_ThenReturnOk() {
        long id = 1;
        UserDto outputDto = new UserDto(id, "name", "mail@mail.ru");
        ResponseEntity<Object> response = new ResponseEntity<>(outputDto, HttpStatus.OK);
        when(userClient.getUser(id)).thenReturn(response);

        String actualOutput = mockMvc.perform(get("/users/{userId}", id))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(actualOutput).isNotNull().isEqualTo(objectMapper.writeValueAsString(outputDto));
        verify(userClient).getUser(id);
        Mockito.verifyNoMoreInteractions(userClient);
    }

    @SneakyThrows
    @Test
    void deleteUser_WhenIdIsNotValid_ThenReturnBadRequest() {
        mockMvc.perform(delete("/users/{userId}", -1))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/users/{userId}", 0))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(userClient);
    }

    @SneakyThrows
    @Test
    void deleteUser_WhenIdIsValid_ThenReturnOk() {
        long id = 1;

        mockMvc.perform(delete("/users/{userId}", id))
                .andExpect(status().isOk());

        verify(userClient).deleteUser(id);
    }

    @SneakyThrows
    @Test
    void updateUser_WhenIdIsNotValid_ThenReturnBadRequest() {
        UserDto inputDto = new UserDto(1, "new", "new@mail.ru");

        mockMvc.perform(patch("/users/{userId}", -1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/users/{userId}", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(userClient);
    }

    @SneakyThrows
    @Test
    void updateUser_WhenEmailNotValid_ThenReturnBadRequest() {
        UserDto inputDto = new UserDto(1, "new", "wrong_mail");

        mockMvc.perform(patch("/users/{userId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(userClient);
    }

    @SneakyThrows
    @Test
    void updateUser_WhenDtoIsValid_ThenReturnOk() {
        long userId = 1;
        UserDto inputDto = new UserDto(1, "new", "new@mail.ru");
        ResponseEntity<Object> response = new ResponseEntity<>(inputDto, HttpStatus.OK);
        when(userClient.updateUser(inputDto, userId)).thenReturn(response);

        String output = mockMvc.perform(patch("/users/{userId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(output)
                .as("Проверка возвращаемого значения при обновлении пользователя")
                .isNotNull()
                .isEqualTo(objectMapper.writeValueAsString(inputDto));
        verify(userClient, Mockito.times(1)).updateUser(inputDto, userId);
    }
}