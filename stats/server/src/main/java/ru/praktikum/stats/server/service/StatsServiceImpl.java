package ru.praktikum.stats.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import ru.praktikum.stats.dto.HitDto;
import ru.praktikum.stats.dto.StatsDto;
import ru.praktikum.stats.server.exception.ValidationException;
import ru.praktikum.stats.server.model.Hit;
import ru.praktikum.stats.server.model.HitMapper;
import ru.praktikum.stats.server.repository.StatsRepository;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;
    private final HitMapper hitMapper;

    @Override
    @Transactional
    public HitDto createHit(HitDto hitDto) {
        Hit hit = hitMapper.toHit(hitDto);

        return hitMapper.toHitDto(statsRepository.save(hit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatsDto> getStats(String start, String end, List<String> uris, String unique) {
        LocalDateTime startDate;
        LocalDateTime endDate;

        try {
            startDate = LocalDateTime.parse(URLDecoder.decode(start, StandardCharsets.UTF_8),
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            endDate = LocalDateTime.parse(URLDecoder.decode(end, StandardCharsets.UTF_8),
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Не верный формат даты.");
        }
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Даты начала и окончания некорректны.");
        }
        boolean onlyUnique = Boolean.valueOf(unique);
        List<StatsDto> stats;

        if (uris != null && !uris.isEmpty()) {
            uris = uris.stream().map(StringUtils::strip).collect(Collectors.toList());
        }

        if (onlyUnique) {
            stats = uris != null && !uris.isEmpty()
                    ? statsRepository.getUniqueWithUris(startDate, endDate, uris)
                    : statsRepository.getUniqueWithOutUris(startDate, endDate);
        } else {
            stats = uris != null && !uris.isEmpty()
                    ? statsRepository.getWithUris(startDate, endDate, uris)
                    : statsRepository.getWithOutUris(startDate, endDate);
        }

        return stats;
    }
}