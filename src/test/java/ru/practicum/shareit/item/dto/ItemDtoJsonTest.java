package ru.practicum.shareit.item.dto;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemDtoJsonTest {
    private final JacksonTester<ItemDto> json;

    @SneakyThrows
    @Test
    void testSerialization() {
        ItemDto itemDto = new ItemDto(1L, "name", "description", true, null);

        JsonContent<ItemDto> result = json.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("name");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("description");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isNull();
    }

    @SneakyThrows
    @Test
    void testDeserialization() {
        String jsonString = "{\"id\":1,\"name\":\"name\",\"description\":\"description\",\"available\":true,\"requestId\":2}";
        ItemDto expectedDto = new ItemDto(1L, "name", "description", true, 2L);

        ItemDto result = json.parseObject(jsonString);

        AssertionsForClassTypes.assertThat(result).isEqualTo(expectedDto);
    }
}