package ru.practicum.shareit.user.dto;

import lombok.*;

import javax.validation.constraints.Email;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private long id;
    private String name;
    @Email
    private String email;
}
