package ru.practicum.shareit.request.dto;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class CreateItemRequestDto {
    private String description;
}
