package ru.praktikum.stats.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.praktikum.stats.dto.NewHitDto;
import ru.praktikum.stats.dto.StatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Component
public class StatisticsClient extends BaseClient {
    private final ObjectMapper mapper = new ObjectMapper();
    private final TypeReference<List<StatsDto>> mapType = new TypeReference<>() {
    };
    public static final DateTimeFormatter date = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public StatisticsClient(@Value("${server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> saveHit(String app,
                                          String uri,
                                          String ip,
                                          LocalDateTime timestamp) {
        NewHitDto newHitDto = new NewHitDto(app, uri, ip, timestamp);
        return post("/hit", newHitDto);
    }

    public Long getStatisticsByEventId(Long eventId) {
        Map<String, Object> parameters = Map.of(
                "start", LocalDateTime.now().minusYears(1000).format(date),
                "end", LocalDateTime.now().plusYears(1000).format(date),
                "uris", List.of("/events/" + eventId),
                "unique", true
        );
        ResponseEntity<Object> response = get("/stats?start={start}&end={end}&uris={uris}&unique={unique}", parameters);

        List<StatsDto> viewStatsList = response.hasBody() ? mapper.convertValue(response.getBody(), mapType) : Collections.emptyList();
        return viewStatsList != null && !viewStatsList.isEmpty() ? viewStatsList.get(0).getHits() : 0L;
    }

    public Map<Long, Long> getSetViewsByEventId(Set<Long> eventIds) {
        Map<String, Object> parameters = Map.of(
                "start", LocalDateTime.now().minusYears(1000).format(date),
                "end", LocalDateTime.now().plusYears(1000).format(date),
                "uris", (eventIds.stream().map(id -> "/events/" + id).collect(Collectors.toList())),
                "unique", Boolean.FALSE
        );
        ResponseEntity<Object> response = get("/stats?start={start}&end={end}&uris={uris}&unique={unique}", parameters);

        return response.hasBody() ? mapper.convertValue(response.getBody(), mapType)
                .stream()
                .collect(Collectors.toMap(this::getEventIdFromURI, StatsDto::getHits))
                : Collections.emptyMap();
    }

    private Long getEventIdFromURI(StatsDto e) {
        return Long.parseLong(e.getUri().substring(e.getUri().lastIndexOf("/") + 1));
    }
}
