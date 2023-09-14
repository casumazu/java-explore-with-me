package ru.praktikum.stats.server.model;

import lombok.experimental.UtilityClass;
import ru.praktikum.stats.dto.HitDto;

@UtilityClass
public class HitMapper {
    public static Hit toHit(HitDto hitDto) {
        return Hit
                .builder()
                .app(hitDto.getApp())
                .uri(hitDto.getUri())
                .ip(hitDto.getIp())
                .created(hitDto.getTimestamp())
                .build();
    }
}
