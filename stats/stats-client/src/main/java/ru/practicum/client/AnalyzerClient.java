package ru.practicum.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.stats.service.dashboard.RecommendationsControllerGrpc.RecommendationsControllerBlockingStub;
import ru.practicum.stats.service.dashboard.RecommendationsProto.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzerClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerBlockingStub stub;

    /**
     * Получить рекомендации для пользователя
     */
    public List<RecommendedEvent> getRecommendationsForUser(Long userId, int maxResults) {
        try {
            UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                    .setUserId(userId)
                    .setMaxResults(maxResults)
                    .build();

            Iterator<RecommendedEventProto> iterator = stub.getRecommendationsForUser(request);
            List<RecommendedEvent> result = toStream(iterator)
                    .map(this::toRecommendedEvent)
                    .collect(Collectors.toList());

            log.info("Получено {} рекомендаций для пользователя {}", result.size(), userId);
            return result;

        } catch (Exception e) {
            log.error("Ошибка получения рекомендаций для пользователя {}: {}", userId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Получить похожие мероприятия
     */
    public List<RecommendedEvent> getSimilarEvents(Long eventId, Long userId, int maxResults) {
        try {
            SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                    .setEventId(eventId)
                    .setUserId(userId)
                    .setMaxResults(maxResults)
                    .build();

            Iterator<RecommendedEventProto> iterator = stub.getSimilarEvents(request);
            List<RecommendedEvent> result = toStream(iterator)
                    .map(this::toRecommendedEvent)
                    .collect(Collectors.toList());

            log.info("Получено {} похожих мероприятий для события {}", result.size(), eventId);
            return result;

        } catch (Exception e) {
            log.error("Ошибка получения похожих мероприятий для события {}: {}", eventId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Получить сумму взаимодействий для списка мероприятий
     */
    public List<RecommendedEvent> getInteractionsCount(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                    .addAllEventId(eventIds)
                    .build();

            Iterator<RecommendedEventProto> iterator = stub.getInteractionsCount(request);
            List<RecommendedEvent> res = toStream(iterator)
                    .map(this::toRecommendedEvent)
                    .toList();

            log.info("Получена сумма взаимодействий для мероприятий");
            return res;

        } catch (Exception e) {
            log.error("Ошибка получения суммы взаимодействий для мероприятий {}: {}", eventIds, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Преобразовать Iterator в Stream
     */
    private java.util.stream.Stream<RecommendedEventProto> toStream(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }

    /**
     * Преобразовать RecommendedEventProto в RecommendedEvent
     */
    private RecommendedEvent toRecommendedEvent(RecommendedEventProto proto) {
        return new RecommendedEvent(proto.getEventId(), proto.getScore());
    }

}