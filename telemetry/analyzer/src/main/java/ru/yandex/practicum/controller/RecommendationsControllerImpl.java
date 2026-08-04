package ru.yandex.practicum.controller;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.stats.service.dashboard.RecommendationsControllerGrpc.RecommendationsControllerImplBase;
import ru.practicum.stats.service.dashboard.RecommendationsProto.*;
import ru.yandex.practicum.service.RecommendationService;

import java.util.List;
import java.util.Map;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class RecommendationsControllerImpl extends RecommendationsControllerImplBase {

    private final RecommendationService recommendationService;

    @Override
    public void getRecommendationsForUser(
            UserPredictionsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {

        try {
            long userId = request.getUserId();
            int maxResults = request.getMaxResults();

            log.info("gRPC: Getting recommendations for user: {}, maxResults: {}", userId, maxResults);

            List<RecommendedEventProto> recommendations =
                    recommendationService.getRecommendationsForUser(userId, maxResults);

            for (RecommendedEventProto event : recommendations) {
                responseObserver.onNext(event);
            }

            responseObserver.onCompleted();
            log.info("gRPC: Completed recommendations for user: {}, returned: {}", userId, recommendations.size());

        } catch (Exception e) {
            log.error("gRPC: Error getting recommendations: {}", e.getMessage(), e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

    @Override
    public void getSimilarEvents(
            SimilarEventsRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {

        try {
            long eventId = request.getEventId();
            long userId = request.getUserId();
            int maxResults = request.getMaxResults();

            log.info("gRPC: Getting similar events: eventId={}, userId={}, maxResults={}",
                    eventId, userId, maxResults);

            List<RecommendedEventProto> similarEvents =
                    recommendationService.getSimilarEvents(eventId, userId, maxResults);

            for (RecommendedEventProto event : similarEvents) {
                responseObserver.onNext(event);
            }

            responseObserver.onCompleted();
            log.info("gRPC: Completed similar events for event: {}, returned: {}", eventId, similarEvents.size());

        } catch (Exception e) {
            log.error("gRPC: Error getting similar events: {}", e.getMessage(), e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

    @Override
    public void getInteractionsCount(
            InteractionsCountRequestProto request,
            StreamObserver<RecommendedEventProto> responseObserver
    ) {

        try {
            List<Long> eventIds = request.getEventIdList();

            log.info("gRPC: Getting interactions count for {} events", eventIds.size());

            Map<Long, Double> interactionsCount =
                    recommendationService.getInteractionsCount(eventIds);

            for (Map.Entry<Long, Double> entry : interactionsCount.entrySet()) {
                RecommendedEventProto proto = RecommendedEventProto.newBuilder()
                        .setEventId(entry.getKey())
                        .setScore(entry.getValue().floatValue())
                        .build();
                responseObserver.onNext(proto);
            }

            responseObserver.onCompleted();
            log.info("gRPC: Completed interactions count for {} events", interactionsCount.size());

        } catch (Exception e) {
            log.error("gRPC: Error getting interactions count: {}", e.getMessage(), e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }
}