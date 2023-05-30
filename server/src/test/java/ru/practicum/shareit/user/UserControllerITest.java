package ru.practicum.shareit.user;

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
import ru.practicum.shareit.user.dto.UserDto;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserControllerITest {
    private final ObjectMapper objectMapper;
    private final MockMvc mockMvc;
    @MockBean
    private UserService userService;

    @SneakyThrows
    @Test
    void createUser_WhenDtoIsValid_ThenReturnOk() {
        UserDto inputDto = new UserDto(0, "name", "mail@mail.ru");
        UserDto outputDto = new UserDto(1, "name", "mail@mail.ru");

        when(userService.createUser(inputDto)).thenReturn(outputDto);

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
        verify(userService, Mockito.times(1)).createUser(inputDto);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @SneakyThrows
    @Test
    void getAll_WhenUsersDoNotExist_ThenReturnOk() {
        ArrayList<UserDto> emptyList = new ArrayList<>();
        when(userService.getAll()).thenReturn(emptyList);

        String output = mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(output)
                .as("Проверка вывода пустого списка при отсутствии пользователей")
                .isNotNull()
                .isEqualTo(objectMapper.writeValueAsString(emptyList));
        verify(userService, Mockito.times(1)).getAll();
        Mockito.verifyNoMoreInteractions(userService);
    }

    @SneakyThrows
    @Test
    void getAll_WhenUsersExist_ThenReturnOk() {
        List<UserDto> users = List.of(new UserDto(1, "name1", "mail1@mail.ru"),
                new UserDto(2, "name2", "mail2@mail.ru"));
        when(userService.getAll()).thenReturn(users);

        String output = mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(output)
                .as("Проверка вывода непустого списка")
                .isNotNull()
                .isEqualTo(objectMapper.writeValueAsString(users));
        verify(userService, Mockito.times(1)).getAll();
        Mockito.verifyNoMoreInteractions(userService);
    }

    @SneakyThrows
    @Test
    void getUser_WhenIdIsValid_ThenReturnOk() {
        long id = 1;
        UserDto outputDto = new UserDto(id, "name", "mail@mail.ru");
        when(userService.getUser(id)).thenReturn(outputDto);

        String actualOutput = mockMvc.perform(get("/users/{userId}", id))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(actualOutput).isNotNull().isEqualTo(objectMapper.writeValueAsString(outputDto));
        verify(userService).getUser(id);
    }

    @SneakyThrows
    @Test
    void deleteUser_WhenIdIsValid_ThenReturnOk() {
        long id = 1;

        mockMvc.perform(delete("/users/{userId}", id))
                .andExpect(status().isOk());

        verify(userService).deleteUser(id);
    }

    @SneakyThrows
    @Test
    void updateUser_WhenDtoIsValid_ThenReturnOk() {
        UserDto inputDto = new UserDto(1, "new", "new@mail.ru");
        when(userService.updateUser(inputDto)).thenReturn(inputDto);

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
        verify(userService, Mockito.times(1)).updateUser(inputDto);
    }
}