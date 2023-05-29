package ru.practicum.shareit.item.dto;

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
class CommentDtoJsonTest {
    private final JacksonTester<CommentDto> json;

    @SneakyThrows
    @Test
    void testSerialization() {
        LocalDateTime time = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        CommentDto commentDto = new CommentDto(1L, "description", "name", time);

        JsonContent<CommentDto> result = json.write(commentDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("description");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("name");
        assertThat(result).extractingJsonPathValue("$.created").isEqualTo(time.toString());
    }

    @SneakyThrows
    @Test
    void testDeserialization() {
        String jsonString = "{\"id\":1,\"text\":\"description\",\"authorName\":\"name\",\"created\":\"2023-05-27T18:30:00\"}";
        LocalDateTime time = LocalDateTime.of(2023, 5, 27, 18, 30);
        CommentDto expectedDto = new CommentDto(1L, "description", "name", time);

        CommentDto result = json.parseObject(jsonString);

        AssertionsForClassTypes.assertThat(result).isEqualTo(expectedDto);
    }
}