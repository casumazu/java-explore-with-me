package ru.praktikum.stats.server.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import ru.praktikum.stats.dto.HitDto;
import ru.praktikum.stats.dto.StatsView;
import ru.praktikum.stats.server.model.Hit;
import ru.praktikum.stats.server.model.HitMapper;
import ru.praktikum.stats.server.repository.StatsRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    public StatsServiceImpl(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    @Override
    @Transactional
    public HitDto addStat(HitDto hitDto) {
       Hit hit = statsRepository.save(HitMapper.toHit(hitDto));
       return new HitDto(hitDto.getId(), hit.getApp(), hit.getUri(), hit.getIp(), hit.getCreated());
    }

    @Transactional
    public List<StatsView> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        List<StatsView> stats;
        if (uris == null || uris.size() == 0) {
            if (unique) {
                stats = statsRepository.countByTimestampUniqueIp(start, end);
            } else {
                stats = statsRepository.countByTimestamp(start, end);
            }
        } else {
            if (unique) {
                stats = statsRepository.findStatWithUnique(start, end, uris);
            } else {
                stats = statsRepository.findStatNotUnique(start, end, uris);
            }
        }
        log.info("Получена статистика по uri: {}", uris);
        return stats;
    }
}
