package ru.praktikum.stats.server.model;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;
import ru.praktikum.stats.dto.HitDto;

@Mapper(componentModel = "spring")
@Component
public interface HitMapper {

    @Mapping(target = "timestamp", source = "timestamp", dateFormat = "yyyy-MM-dd HH:mm:ss")
    HitDto toHitDto(Hit hit);

    @Mapping(target = "timestamp", source = "timestamp", dateFormat = "yyyy-MM-dd HH:mm:ss")
    Hit toHit(HitDto hitDto);
}
