package ru.practicum.shareit.request.dto;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestWithAnswersDtoTest {
    private final JacksonTester<ItemRequestWithAnswersDto> json;

    @SneakyThrows
    @Test
    void testSerialization() {
        LocalDateTime time = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        ItemRequestWithAnswersDto itemRequestWithAnswersDto = new ItemRequestWithAnswersDto(1L,
                "description", time, List.of(new ItemDto(1, "name", "description",
                true, 1L)));

        JsonContent<ItemRequestWithAnswersDto> result = json.write(itemRequestWithAnswersDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("description");
        assertThat(result).extractingJsonPathValue("$.created").isEqualTo(time.toString());
        assertThat(result).extractingJsonPathArrayValue("$.items").hasSize(1);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("name");
        assertThat(result).extractingJsonPathStringValue("$.items[0].description").isEqualTo("description");
        assertThat(result).extractingJsonPathBooleanValue("$.items[0].available").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].requestId").isEqualTo(1);
    }

    @SneakyThrows
    @Test
    void testDeserialization() {
        String jsonString = "{\"id\":1,\"description\":\"description\",\"created\":\"2023-05-27T18:30:00\"," +
                "\"items\":[{\"id\":1,\"name\":\"name\",\"description\":\"description\",\"available\":true,\"requestId\":1}]}";
        LocalDateTime time = LocalDateTime.of(2023, 5, 27, 18, 30);
        ItemRequestWithAnswersDto expectedDto = new ItemRequestWithAnswersDto(1L,
                "description", time, List.of(new ItemDto(1, "name", "description",
                true, 1L)));

        ItemRequestWithAnswersDto result = json.parseObject(jsonString);

        AssertionsForClassTypes.assertThat(result).isEqualTo(expectedDto);
    }
}