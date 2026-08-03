package ru.yandex.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.model.UserAction;

import java.time.Instant;

@UtilityClass
public class UserActionMapper {

    public static UserAction toEntity(UserActionAvro avro) {
        return UserAction.builder()
                .userId(avro.getUserId())
                .eventId(avro.getEventId())
                .rating(toRating(avro.getActionType()))
                .timestamp(Instant.ofEpochSecond(avro.getTimestamp()))
                .build();
    }

    private static Float toRating(ActionTypeAvro type) {
        return switch (type) {
            case VIEW -> 0.4F;
            case REGISTER -> 0.8F;
            case LIKE -> 1.0F;
        };
    }
}
