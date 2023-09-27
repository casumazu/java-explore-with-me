package ru.praktikum.stats.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;
    private final HitMapper hitMapper;

    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
            startDate = LocalDateTime.parse(URLDecoder.decode(start, StandardCharsets.UTF_8), dateTimeFormatter);
            endDate = LocalDateTime.parse(URLDecoder.decode(end, StandardCharsets.UTF_8), dateTimeFormatter);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Некорректный формат даты.");
        }
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Даты начала и окончания некорректны.");
        }
        boolean onlyUnique = Boolean.parseBoolean(unique);
        if (onlyUnique) {
            if (uris != null && !uris.isEmpty()) {
                uris.replaceAll(s -> s.replace("[", ""));
                uris.replaceAll(s -> s.replace("]", ""));
                return statsRepository.getUniqueWithUris(startDate, endDate, uris);
            } else {
                return statsRepository.getUniqueWithOutUris(startDate, endDate);
            }
        } else {
            if (uris != null && !uris.isEmpty()) {
                uris.replaceAll(s -> s.replace("[", ""));
                uris.replaceAll(s -> s.replace("]", ""));
                return statsRepository.getWithUris(startDate, endDate, uris);
            } else {
                return statsRepository.getWithOutUris(startDate, endDate);
            }
        }
    }
}