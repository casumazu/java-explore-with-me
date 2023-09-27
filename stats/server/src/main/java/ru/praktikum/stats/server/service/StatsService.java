package ru.praktikum.stats.server.service;

import ru.praktikum.stats.dto.HitDto;
import ru.praktikum.stats.dto.StatsDto;

import java.util.List;

public interface StatsService {


    HitDto createHit(HitDto hitDto);

    List<StatsDto> getStats(String start, String end, List<String> uris, String unique);
}