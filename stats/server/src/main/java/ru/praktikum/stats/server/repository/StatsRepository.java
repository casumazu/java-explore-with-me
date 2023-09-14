package ru.praktikum.stats.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.praktikum.stats.dto.StatsView;
import ru.praktikum.stats.server.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<Hit, Long> {
    @Query("SELECT h.app AS app, h.uri AS uri, count(h.ip) AS hits " +
            "FROM Hit h " +
            "WHERE h.created BETWEEN :start AND :end " +
            "GROUP BY h.app, h.uri, h.ip ORDER BY hits DESC "
    )
    List<StatsView> countByTimestamp(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT h.app AS app, h.uri AS uri, count(DISTINCT h.ip) AS hits " +
            "FROM Hit h " +
            "WHERE h.created BETWEEN :start AND :end " +
            "GROUP BY h.app, h.uri, h.ip " +
            "ORDER BY hits DESC "
    )
    List<StatsView> countByTimestampUniqueIp(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT h.app AS app, h.uri AS uri, count(DISTINCT h.ip) AS hits " +
            "FROM Hit h " +
            "WHERE h.created BETWEEN ?1 AND ?2 " +
            "AND h.uri IN (?3) " +
            "GROUP BY h.app, h.uri ORDER BY hits DESC ")
    List<StatsView> findStatWithUnique(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("uris") List<String> uris);

    @Query("SELECT h.app AS app, h.uri AS uri, count(h.ip) AS hits " +
            "FROM Hit h " +
            "WHERE h.created BETWEEN ?1 AND ?2 " +
            "AND h.uri IN (?3) " +
            "GROUP BY h.app, h.uri ORDER BY hits DESC ")
    List<StatsView> findStatNotUnique(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("uris") List<String> uris);
}