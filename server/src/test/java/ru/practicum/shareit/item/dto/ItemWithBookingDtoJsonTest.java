package ru.practicum.shareit.item.dto;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.SimpleBookingDto;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemWithBookingDtoJsonTest {
    private final JacksonTester<ItemWithBookingDto> json;

    @SneakyThrows
    @Test
    void testSerialization() {
        LocalDateTime start = LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime end = LocalDateTime.now().plusHours(3).truncatedTo(ChronoUnit.SECONDS);
        ItemWithBookingDto itemDto = new ItemWithBookingDto(1L, "name", "description",
                true, null, null, new SimpleBookingDto(1L, start, end,
                Status.APPROVED, 2L), Collections.emptyList());

        JsonContent<ItemWithBookingDto> result = json.write(itemDto);
        System.out.println(result);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("name");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("description");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isNull();
        assertThat(result).extractingJsonPathValue("$.lastBooking").isNull();
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.id").isEqualTo(1);
        assertThat(result).extractingJsonPathValue("$.nextBooking.start").isEqualTo(start.toString());
        assertThat(result).extractingJsonPathValue("$.nextBooking.end").isEqualTo(end.toString());
        assertThat(result).extractingJsonPathValue("$.nextBooking.status").isEqualTo(Status.APPROVED.toString());
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.bookerId").isEqualTo(2);
        assertThat(result).extractingJsonPathArrayValue("$.comments").isEmpty();
    }

    @SneakyThrows
    @Test
    void testDeserialization() {
        String jsonString = "{\"id\":1,\"name\":\"name\",\"description\":\"description\",\"available\":true," +
                "\"requestId\":null,\"nextBooking\":null,\"lastBooking\":{\"id\":1,\"start\":\"2023-05-27T18:30:00\"," +
                "\"end\":\"2023-05-28T18:30:00\",\"status\":\"APPROVED\",\"bookerId\":2},\"comments\":[]}";
        LocalDateTime start = LocalDateTime.of(2023, 5, 27, 18, 30);
        LocalDateTime end = LocalDateTime.of(2023, 5, 28, 18, 30);
        ItemWithBookingDto expectedDto = new ItemWithBookingDto(1L, "name", "description",
                true, null, new SimpleBookingDto(1L, start, end, Status.APPROVED, 2L),
                null, Collections.emptyList());

        ItemWithBookingDto result = json.parseObject(jsonString);

        AssertionsForClassTypes.assertThat(result).isEqualTo(expectedDto);
    }
}