package ru.practicum.shareit.request.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class CreateItemRequestDto {
    @NotBlank
    private String description;
}
