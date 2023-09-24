package ru.praktikum.stats.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ru.praktikum.stats.dto.StatsDto;
import ru.praktikum.stats.server.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<Hit, Long> {
    @Query(value = "SELECT new ru.praktikum.stats.dto.StatsDto(h.app, h.uri, COUNT(DISTINCT h.ip)) " +
            "FROM Hit AS h " +
            "WHERE (h.timestamp between :start AND :end) AND h.uri IN :uris " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC ")
    List<StatsDto> getUniqueWithUris(@Param("start") LocalDateTime timeStart, @Param("end") LocalDateTime timeEnd,
                                     @Param("uris") List<String> uris);

    @Query(value = "SELECT new ru.praktikum.stats.dto.StatsDto(h.app, h.uri, COUNT(DISTINCT h.ip)) " +
            "FROM Hit AS h " +
            "WHERE h.timestamp between :start AND :end " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC ")
    List<StatsDto> getUniqueWithOutUris(@Param("start") LocalDateTime timeStart, @Param("end") LocalDateTime timeEnd);

    @Query(value = "SELECT new ru.praktikum.stats.dto.StatsDto(h.app, h.uri, COUNT(h.ip)) " +
            "FROM Hit AS h " +
            "WHERE (h.timestamp between :start AND :end) AND h.uri IN :uris " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h.ip) DESC ")
    List<StatsDto> getWithUris(@Param("start") LocalDateTime timeStart, @Param("end") LocalDateTime timeEnd,
                               @Param("uris") List<String> uris);

    @Query(value = "SELECT new ru.praktikum.stats.dto.StatsDto(h.app, h.uri, COUNT(h.ip)) " +
            "FROM Hit AS h " +
            "WHERE h.timestamp between :start AND :end " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h.ip) DESC ")
    List<StatsDto> getWithOutUris(@Param("start") LocalDateTime timeStart, @Param("end") LocalDateTime timeEnd);
}
