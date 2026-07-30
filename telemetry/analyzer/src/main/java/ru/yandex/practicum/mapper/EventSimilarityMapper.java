package ru.yandex.practicum.mapper;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.model.EventSimilarity;

import java.time.Instant;

public class EventSimilarityMapper {

    public static EventSimilarity toEntity(EventSimilarityAvro avro) {
        return EventSimilarity.builder()
                .event1(avro.getEventA())
                .event2(avro.getEventB())
                .similarity(avro.getScore())
                .timestamp(Instant.ofEpochSecond(avro.getTimestamp()))
                .build();
    }
}
