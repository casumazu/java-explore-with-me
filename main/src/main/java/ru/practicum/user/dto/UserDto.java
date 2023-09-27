package ru.practicum.user.dto;

import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Valid
public class UserDto {

    private Long id;

    @NotBlank
    private String name;

    @Email
    private String email;
}
