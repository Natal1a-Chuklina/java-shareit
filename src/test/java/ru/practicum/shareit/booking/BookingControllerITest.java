package ru.practicum.shareit.booking;

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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.utils.Constants.HEADER_WITH_USER_ID_NAME;

@WebMvcTest(BookingController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingControllerITest {
    private final ObjectMapper objectMapper;
    private final MockMvc mockMvc;
    @MockBean
    private BookingService bookingService;

    @SneakyThrows
    @Test
    void createBooking_WhenNoRequestHeader_ThenReturnBadRequest() {
        BookingCreationDto input = new BookingCreationDto(1L, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(bookingService);
    }

    @SneakyThrows
    @Test
    void createBooking_WhenNotValidRequestHeader_ThenReturnBadRequest() {
        BookingCreationDto input = new BookingCreationDto(1L, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3));

        mockMvc.perform(post("/bookings")
                        .header(HEADER_WITH_USER_ID_NAME, -1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/bookings")
                        .header(HEADER_WITH_USER_ID_NAME, 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(bookingService);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("wrongBookingDtosStream")
    void createItem_WhenNotValidRequestBody_ThenReturnBadRequest(BookingCreationDto input) {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input))
                        .header(HEADER_WITH_USER_ID_NAME, 1))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(bookingService);
    }

    @SneakyThrows
    @Test
    void createItem_WhenAllParametersValid_ThenReturnOk() {
        BookingCreationDto input = new BookingCreationDto(1L, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3));
        BookingDto output = new BookingDto(1L, input.getStart(), input.getEnd(), Status.WAITING,
                new UserDto(1, "name", "mail@mail.ru"),
                new ItemDto(1, "name", "description", true, null));
        when(bookingService.createBooking(input, 1)).thenReturn(output);

        String actualOutput = mockMvc.perform(post("/bookings")
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(actualOutput)
                .as("Проверка возвращаемого значения при создании бронирования")
                .isNotNull()
                .isEqualTo(objectMapper.writeValueAsString(output));
        verify(bookingService, Mockito.times(1)).createBooking(input, 1);
        Mockito.verifyNoMoreInteractions(bookingService);
    }

    @SneakyThrows
    @Test
    void setBookingStatus_WhenNoRequestHeader_ThenReturnBadRequest() {
        mockMvc.perform(patch("/bookings/{bookingId}", 1)
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(bookingService);
    }

    @SneakyThrows
    @Test
    void setBookingStatus_WhenNotValidRequestHeader_ThenReturnBadRequest() {
        mockMvc.perform(patch("/bookings/{bookingId}", 1)
                        .header(HEADER_WITH_USER_ID_NAME, -1)
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/bookings/{bookingId}", 1)
                        .header(HEADER_WITH_USER_ID_NAME, 0)
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(bookingService);
    }

    @SneakyThrows
    @Test
    void setBookingStatus_WhenNotValidPathVariable_ThenReturnBadRequest() {
        mockMvc.perform(patch("/bookings/{bookingId}", -1)
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/bookings/{bookingId}", 0)
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(bookingService);
    }

    @SneakyThrows
    @Test
    void setBookingStatus_WhenAllParametersValid_ThenReturnOk() {
        BookingDto output = new BookingDto(1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3),
                Status.WAITING, new UserDto(1, "name", "mail@mail.ru"),
                new ItemDto(1, "name", "description", true, null));
        when(bookingService.setBookingStatus(1, 1, true)).thenReturn(output);

        String actualOutput = mockMvc.perform(patch("/bookings/{bookingId}", 1)
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(actualOutput)
                .as("Проверка возвращаемого значения при установке статуса бронирования")
                .isNotNull()
                .isEqualTo(objectMapper.writeValueAsString(output));
        verify(bookingService, Mockito.times(1)).setBookingStatus(1, 1,
                true);
        Mockito.verifyNoMoreInteractions(bookingService);
    }

    @SneakyThrows
    @Test
    void getBooking_WhenNoRequestHeader_ThenReturnBadRequest() {
        mockMvc.perform(get("/bookings/{bookingId}", 1))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(bookingService);
    }

    @SneakyThrows
    @Test
    void getBooking_WhenNotValidRequestHeader_ThenReturnBadRequest() {
        mockMvc.perform(get("/bookings/{bookingId}", 1)
                        .header(HEADER_WITH_USER_ID_NAME, -1))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/bookings/{bookingId}", 1)
                        .header(HEADER_WITH_USER_ID_NAME, 0))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(bookingService);
    }

    @SneakyThrows
    @Test
    void getBooking_WhenNotValidPathVariable_ThenReturnBadRequest() {
        mockMvc.perform(get("/bookings/{bookingId}", -1)
                        .header(HEADER_WITH_USER_ID_NAME, 1))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/bookings/{bookingId}", 0)
                        .header(HEADER_WITH_USER_ID_NAME, 1))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(bookingService);
    }

    @SneakyThrows
    @Test
    void getBooking_WhenAllParametersValid_ThenReturnOk() {
        BookingDto output = new BookingDto(1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3),
                Status.WAITING, new UserDto(1, "name", "mail@mail.ru"),
                new ItemDto(1, "name", "description", true, null));
        when(bookingService.getBooking(1, 1)).thenReturn(output);

        String actualOutput = mockMvc.perform(get("/bookings/{bookingId}", 1)
                        .header(HEADER_WITH_USER_ID_NAME, 1))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(actualOutput)
                .as("Проверка возвращаемого значения при получении бронирования по id")
                .isNotNull()
                .isEqualTo(objectMapper.writeValueAsString(output));
        verify(bookingService, Mockito.times(1)).getBooking(1, 1);
        Mockito.verifyNoMoreInteractions(bookingService);
    }

    @SneakyThrows
    @Test
    void getBookingsByBookerId_WhenNoRequestHeader_ThenReturnBadRequest() {
        mockMvc.perform(get("/bookings")
                        .param("state", "state")
                        .param("from", "0")
                        .param("size", "5"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(bookingService);
    }

    @SneakyThrows
    @Test
    void getBookingsByBookerId_WhenNotValidRequestHeader_ThenReturnBadRequest() {
        mockMvc.perform(get("/bookings")
                        .header(HEADER_WITH_USER_ID_NAME, -1)
                        .param("state", "state")
                        .param("from", "0")
                        .param("size", "5"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/bookings")
                        .header(HEADER_WITH_USER_ID_NAME, 0)
                        .param("state", "state")
                        .param("from", "0")
                        .param("size", "5"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(bookingService);
    }

    @SneakyThrows
    @Test
    void getBookingsByBookerId_WhenNotValidRequestParams_ThenReturnBadRequest() {
        mockMvc.perform(get("/bookings")
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .param("state", "state")
                        .param("from", "-1")
                        .param("size", "5"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/bookings")
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .param("state", "state")
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/bookings")
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .param("state", "state")
                        .param("from", "0")
                        .param("size", "-1"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(bookingService);
    }

    @SneakyThrows
    @Test
    void getBookingsByBookerId_WhenNoRequestParams_ThenReturnOk() {
        List<BookingDto> output = List.of(new BookingDto(1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3),
                Status.WAITING, new UserDto(1, "name", "mail@mail.ru"),
                new ItemDto(1, "name", "description", true, null)));
        when(bookingService.getBookingsByBookerId(1, "ALL", 0, 20)).thenReturn(output);

        String actualOutput = mockMvc.perform(get("/bookings")
                        .header(HEADER_WITH_USER_ID_NAME, 1))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(actualOutput)
                .as("Проверка возвращаемого значения при получении бронирований пользователя")
                .isNotNull()
                .isEqualTo(objectMapper.writeValueAsString(output));
        verify(bookingService, Mockito.times(1)).getBookingsByBookerId(1, "ALL",
                0, 20);
        Mockito.verifyNoMoreInteractions(bookingService);
    }

    @SneakyThrows
    @Test
    void getBookingsByBookerId_WhenRequestParamsExist_ThenReturnOk() {
        mockMvc.perform(get("/bookings")
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .param("state", "state")
                        .param("from", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(bookingService, Mockito.times(1)).getBookingsByBookerId(1, "state",
                1, 5);
        Mockito.verifyNoMoreInteractions(bookingService);
    }

    @SneakyThrows
    @Test
    void getBookingsByOwnerId_WhenNoRequestHeader_ThenReturnBadRequest() {
        mockMvc.perform(get("/bookings/owner")
                        .param("state", "state")
                        .param("from", "0")
                        .param("size", "5"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(bookingService);
    }

    @SneakyThrows
    @Test
    void getBookingsByOwnerId_WhenNotValidRequestHeader_ThenReturnBadRequest() {
        mockMvc.perform(get("/bookings/owner")
                        .header(HEADER_WITH_USER_ID_NAME, -1)
                        .param("state", "state")
                        .param("from", "0")
                        .param("size", "5"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/bookings/owner")
                        .header(HEADER_WITH_USER_ID_NAME, 0)
                        .param("state", "state")
                        .param("from", "0")
                        .param("size", "5"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(bookingService);
    }

    @SneakyThrows
    @Test
    void getBookingsByOwnerId_WhenNotValidRequestParams_ThenReturnBadRequest() {
        mockMvc.perform(get("/bookings/owner")
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .param("state", "state")
                        .param("from", "-1")
                        .param("size", "5"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/bookings/owner")
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .param("state", "state")
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/bookings/owner")
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .param("state", "state")
                        .param("from", "0")
                        .param("size", "-1"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(bookingService);
    }

    @SneakyThrows
    @Test
    void getBookingsByOwnerId_WhenNoRequestParams_ThenReturnOk() {
        List<BookingDto> output = List.of(new BookingDto(1L, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3),
                Status.WAITING, new UserDto(1, "name", "mail@mail.ru"),
                new ItemDto(1, "name", "description", true, null)));
        when(bookingService.getBookingsByOwnerId(1, "ALL", 0, 20)).thenReturn(output);

        String actualOutput = mockMvc.perform(get("/bookings/owner")
                        .header(HEADER_WITH_USER_ID_NAME, 1))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(actualOutput)
                .as("Проверка возвращаемого значения при получении бронирований пользователя")
                .isNotNull()
                .isEqualTo(objectMapper.writeValueAsString(output));
        verify(bookingService, Mockito.times(1)).getBookingsByOwnerId(1, "ALL",
                0, 20);
        Mockito.verifyNoMoreInteractions(bookingService);
    }

    @SneakyThrows
    @Test
    void getBookingsByOwnerId_WhenRequestParamsExist_ThenReturnOk() {
        mockMvc.perform(get("/bookings/owner")
                        .header(HEADER_WITH_USER_ID_NAME, 1)
                        .param("state", "state")
                        .param("from", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(bookingService, Mockito.times(1)).getBookingsByOwnerId(1, "state",
                1, 5);
        Mockito.verifyNoMoreInteractions(bookingService);
    }

    static Stream<BookingCreationDto> wrongBookingDtosStream() {
        BookingCreationDto nullIdDto = new BookingCreationDto(null, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3));
        BookingCreationDto nullStartDto = new BookingCreationDto(1L, null,
                LocalDateTime.now().plusHours(3));
        BookingCreationDto nullEndDto = new BookingCreationDto(1L, LocalDateTime.now().plusHours(1), null);
        BookingCreationDto startInPastDto = new BookingCreationDto(1L, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusHours(3));
        BookingCreationDto endInPastDto = new BookingCreationDto(1L, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().minusDays(1));

        return Stream.of(nullIdDto, nullStartDto, nullEndDto, startInPastDto, endInPastDto);
    }

}