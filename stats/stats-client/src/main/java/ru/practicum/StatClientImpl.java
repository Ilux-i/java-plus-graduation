package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import ru.practicum.config.StatsServiceInfo;
import ru.practicum.ewm.HitDto;
import ru.practicum.ewm.StatRequestParamDto;
import ru.practicum.ewm.StatResponseDto;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class StatClientImpl implements StatClient {
    private final RestClient restClient;
    private final StatsServiceInfo statsServiceInfo;

    public StatClientImpl(StatsServiceInfo statsServiceInfo) {
        this.statsServiceInfo = statsServiceInfo;
        this.restClient = RestClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public HitDto postHit(HitDto dto) {
        try {
            URI uri = statsServiceInfo.getUri("/hit");
            return restClient.post()
                    .uri(uri)
                    .contentType(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .body(dto)
                    .retrieve()
                    .body(HitDto.class);
        } catch (Exception e) {
            log.error("Неудачная попытка добавления записи в сервис статистики. Запись: {}", dto);
            return new HitDto();
        }
    }

    @Override
    public List<StatResponseDto> getStats(StatRequestParamDto dto) {
        try {
            URI baseUri = statsServiceInfo.getUri("/stats");
            URI finalUri = UriComponentsBuilder.fromUri(baseUri)
                    .queryParam("start", dto.getStart())
                    .queryParam("end", dto.getEnd())
                    .queryParam("uris", dto.getUris())
                    .queryParam("unique", dto.getUnique())
                    .build()
                    .toUri();

            return restClient.get()
                    .uri(finalUri)
                    .accept(APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<StatResponseDto>>() {});
        } catch (Exception e) {
            log.error("Неудачная попытка получения данных статистики из сервиса статистики. " +
                    "Параметры запроса: {}", dto);
            return new ArrayList<>();
        }
    }

}