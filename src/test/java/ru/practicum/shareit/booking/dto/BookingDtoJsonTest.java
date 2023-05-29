package ru.practicum.shareit.booking.dto;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingDtoJsonTest {
    private final JacksonTester<BookingDto> json;

    @SneakyThrows
    @Test
    void testSerialization() {
        LocalDateTime start = LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime end = LocalDateTime.now().plusHours(3).truncatedTo(ChronoUnit.SECONDS);
        BookingDto bookingDto = new BookingDto(1L, start, end, Status.WAITING, new UserDto(1, "name",
                "mail@mail.ru"), new ItemDto(1, "name", "description", true, null));

        JsonContent<BookingDto> result = json.write(bookingDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathValue("$.start").isEqualTo(start.toString());
        assertThat(result).extractingJsonPathValue("$.end").isEqualTo(end.toString());
        assertThat(result).extractingJsonPathValue("$.status").isEqualTo(Status.WAITING.toString());
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.booker.name").isEqualTo("name");
        assertThat(result).extractingJsonPathStringValue("$.booker.email").isEqualTo("mail@mail.ru");
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("name");
        assertThat(result).extractingJsonPathStringValue("$.item.description").isEqualTo("description");
        assertThat(result).extractingJsonPathBooleanValue("$.item.available").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.item.requestId").isNull();
    }

    @SneakyThrows
    @Test
    void testDeserialization() {
        String jsonString = "{\"id\":1,\"start\":\"2023-05-27T18:30:00\",\"end\":\"2023-05-28T18:30:00\"," +
                "\"status\":\"WAITING\",\"booker\":{\"id\":1,\"name\":\"name\",\"email\":\"mail@mail.ru\"}," +
                "\"item\":{\"id\":1,\"name\":\"name\",\"description\":\"description\",\"available\":true,\"requestId\":null}}";
        LocalDateTime start = LocalDateTime.of(2023, 5, 27, 18, 30);
        LocalDateTime end = LocalDateTime.of(2023, 5, 28, 18, 30);
        BookingDto expectedDto = new BookingDto(1L, start, end, Status.WAITING, new UserDto(1, "name",
                "mail@mail.ru"), new ItemDto(1, "name", "description", true, null));

        BookingDto result = json.parseObject(jsonString);

        AssertionsForClassTypes.assertThat(result).isEqualTo(expectedDto);
    }
}