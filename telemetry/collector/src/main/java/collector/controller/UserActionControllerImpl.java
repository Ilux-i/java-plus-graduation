package collector.controller;

import collector.mapper.UserActionProtoMapper;
import collector.service.KafkaProducerService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.stats.service.collector.UserActionControllerGrpc.UserActionControllerImplBase;
import ru.practicum.stats.service.collector.UserActionOuterClass.UserActionProto;
import com.google.protobuf.Empty;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserActionControllerImpl extends UserActionControllerImplBase {

    private final UserActionProtoMapper userActionProtoMapper;
    private final KafkaProducerService kafkaProducerService;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        try {
            // Конвертация из Proto в Avro
            UserActionAvro avroMessage = userActionProtoMapper.mapFromProto(request);

            // Отправка в Kafka
            kafkaProducerService.sendUserAction(avroMessage);

            log.info("Обработано действие пользователя: userId={}, eventId={}, actionType={}",
                    avroMessage.getUserId(),
                    avroMessage.getEventId(),
                    avroMessage.getActionType());

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            log.warn("Ошибка валидации: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Validation error: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        } catch (Exception e) {
            log.error("Ошибка обработки действия пользователя", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

}
