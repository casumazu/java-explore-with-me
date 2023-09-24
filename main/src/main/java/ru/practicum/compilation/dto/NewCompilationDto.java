package ru.practicum.compilation.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NewCompilationDto {

    private Set<Long> events;
    private Boolean pinned;

    @NotBlank
    @Size(max = 50)
    private String title;
}
