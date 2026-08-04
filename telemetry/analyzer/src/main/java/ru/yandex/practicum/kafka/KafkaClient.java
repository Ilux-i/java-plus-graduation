package ru.yandex.practicum.kafka;

import deserializer.BaseAvroDeserializer;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;

public interface KafkaClient<T extends SpecificRecordBase> {

    Consumer<String, T> getConsumer(String groupId, Class<? extends BaseAvroDeserializer<T>> deserializer);

}
