package ru.practicum.shareit.booking.dto;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingCreationDtoJsonTest {
    private final JacksonTester<BookingCreationDto> json;

    @SneakyThrows
    @Test
    void testSerialization() {
        LocalDateTime start = LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime end = LocalDateTime.now().plusHours(3).truncatedTo(ChronoUnit.SECONDS);
        BookingCreationDto bookingCreationDto = new BookingCreationDto(1L, start, end);

        JsonContent<BookingCreationDto> result = json.write(bookingCreationDto);

        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathValue("$.start").isEqualTo(start.toString());
        assertThat(result).extractingJsonPathValue("$.end").isEqualTo(end.toString());
    }

    @SneakyThrows
    @Test
    void testDeserialization() {
        String jsonString = "{\"itemId\":1,\"start\":\"2023-05-27T18:30:00\",\"end\":\"2023-05-28T18:30:00\"}";
        LocalDateTime start = LocalDateTime.of(2023, 5, 27, 18, 30);
        LocalDateTime end = LocalDateTime.of(2023, 5, 28, 18, 30);
        BookingCreationDto expectedDto = new BookingCreationDto(1L, start, end);

        BookingCreationDto result = json.parseObject(jsonString);

        AssertionsForClassTypes.assertThat(result).isEqualTo(expectedDto);
    }
}