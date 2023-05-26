package ru.practicum.shareit.request.dto;

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
class CreateItemRequestDtoJsonTest {
    private final JacksonTester<CreateItemRequestDto> json;

    @SneakyThrows
    @Test
    void testSerialization() {
        CreateItemRequestDto createItemRequestDto = new CreateItemRequestDto("description");

        JsonContent<CreateItemRequestDto> result = json.write(createItemRequestDto);

        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("description");
    }

    @SneakyThrows
    @Test
    void testDeserialization() {
        String jsonString = "{\"description\":\"description\"}";
        CreateItemRequestDto expectedDto = new CreateItemRequestDto("description");

        CreateItemRequestDto result = json.parseObject(jsonString);

        AssertionsForClassTypes.assertThat(result).isEqualTo(expectedDto);
    }
}