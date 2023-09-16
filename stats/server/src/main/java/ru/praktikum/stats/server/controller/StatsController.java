package ru.praktikum.stats.server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.praktikum.stats.dto.HitDto;
import ru.praktikum.stats.dto.StatsView;
import ru.praktikum.stats.server.service.StatsService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@Validated
@RequestMapping
public class StatsController {
    private final StatsService statsService;

    @Autowired
    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @PostMapping("/hit")
    public ResponseEntity<HitDto> saveStat(@RequestBody @Valid HitDto endpointHit) {
        log.info("Получен GET-запрос на сохранение информации об обращении к эндпоинту {}", endpointHit.getUri());
        endpointHit.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(statsService.addStat(endpointHit));
    }

    @GetMapping("/stats")
    public List<StatsView> getStat(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                   LocalDateTime start,
                                   @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                   LocalDateTime end,
                                   @RequestParam(required = false) List<String> uris,
                                   @RequestParam(defaultValue = "false") boolean unique) {
        log.info("Получен GET-запрос на получение статистики");
        return statsService.getStats(start, end, uris, unique);
    }
}