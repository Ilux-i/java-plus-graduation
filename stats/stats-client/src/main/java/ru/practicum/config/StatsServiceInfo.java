package ru.practicum.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;

@Slf4j
@Component
public class StatsServiceInfo {

    private final DiscoveryClient discoveryClient;
    private final String statsServiceId;

    private String baseUrl;
    private ServiceInstance currentInstance;

    public StatsServiceInfo(
            @Value("${stats.service.id:STATS-SERVER}") String statsServiceId,
            DiscoveryClient discoveryClient
    ) {
        this.statsServiceId = statsServiceId;
        this.discoveryClient = discoveryClient;
        log.info("StatsServiceInfo создан. Сервис статистики: {}", statsServiceId);
    }

    private String getBaseUrl() {
        if (baseUrl != null) {
            return baseUrl;
        }

        log.info("Первый запрос к StatsServiceInfo. Ищем сервис: {}", statsServiceId);

        List<ServiceInstance> instances = discoveryClient.getInstances(statsServiceId);

        if (instances == null || instances.isEmpty()) {
            log.warn("Сервис '{}' не найден в Eureka.", statsServiceId);
            throw new RuntimeException("Сервис статистики не найден в Eureka");
        }

        ServiceInstance instance = instances.getFirst();
        currentInstance = instance;
        baseUrl = "http://" + instance.getHost() + ":" + instance.getPort();

        log.info("Stats Server найден: {}", baseUrl);
        return baseUrl;
    }

    public URI getUri(String path) {
        return URI.create(getBaseUrl() + path);
    }
}