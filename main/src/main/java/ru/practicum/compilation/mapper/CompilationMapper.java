package ru.practicum.compilation.mapper;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.model.Compilation;

@Component
@Mapper(componentModel = "spring")
public interface CompilationMapper {
    CompilationDto toCompilationDto(Compilation compilation);

    default Compilation toCompilation(NewCompilationDto dto) {
        Compilation entity = new Compilation();
        entity.setPinned(dto.getPinned() != null && dto.getPinned());
        entity.setTitle(dto.getTitle());
        return entity;
    }
}
