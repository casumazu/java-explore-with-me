package ru.praktikum.stats.server.service;

import ru.praktikum.stats.dto.HitDto;
import ru.praktikum.stats.dto.StatsView;


import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    HitDto addStat(HitDto addStat);

    List<StatsView> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
