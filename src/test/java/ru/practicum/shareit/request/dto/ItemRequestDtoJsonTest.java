package ru.practicum.shareit.request.dto;

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
class ItemRequestDtoJsonTest {
    private final JacksonTester<ItemRequestDto> json;

    @SneakyThrows
    @Test
    void testSerialization() {
        LocalDateTime time = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        ItemRequestDto itemRequestDto = new ItemRequestDto(1L, "description", time);

        JsonContent<ItemRequestDto> result = json.write(itemRequestDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("description");
        assertThat(result).extractingJsonPathValue("$.created").isEqualTo(time.toString());
    }

    @SneakyThrows
    @Test
    void testDeserialization() {
        String jsonString = "{\"id\":1,\"description\":\"description\",\"created\":\"2023-05-27T18:30:00\"}";
        LocalDateTime time = LocalDateTime.of(2023, 5, 27, 18, 30);
        ItemRequestDto expectedDto = new ItemRequestDto(1L, "description", time);

        ItemRequestDto result = json.parseObject(jsonString);

        AssertionsForClassTypes.assertThat(result).isEqualTo(expectedDto);
    }
}