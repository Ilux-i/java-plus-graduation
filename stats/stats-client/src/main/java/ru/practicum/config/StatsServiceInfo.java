package ru.practicum.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.event.EventListener;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;

@Slf4j
@Component
public class StatsServiceInfo {

    private final DiscoveryClient discoveryClient;
    private final RetryTemplate retryTemplate;
    private final String statsServiceId;

    @Getter
    private volatile ServiceInstance currentInstance;

    @Getter
    private volatile String baseUrl;

    public StatsServiceInfo(
            @Value("${stats.service.id:STATS-SERVER}") String statsServiceId,
            DiscoveryClient discoveryClient,
            RetryTemplate retryTemplate
    ) {
        this.statsServiceId = statsServiceId;
        this.discoveryClient = discoveryClient;
        this.retryTemplate = retryTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        log.info("Инициализация StatsServiceInfo: запрос актуального пути в Discovery");
        refreshServiceInfo();
    }

    private void refreshServiceInfo() {
        try {
            ServiceInstance instance = retryTemplate.execute(context -> {
                log.debug("Запрос к DiscoveryClient для сервиса: {}", statsServiceId);
                List<ServiceInstance> instances = discoveryClient.getInstances(statsServiceId);

                if (instances == null || instances.isEmpty()) {
                    throw new RuntimeException(
                            "Сервис статистики с id '" + statsServiceId + "' не найден в DiscoveryClient"
                    );
                }

                ServiceInstance found = instances.getFirst();
                log.info("Найден экземпляр Stats Server: host={}, port={}, uri={}",
                        found.getHost(), found.getPort(), found.getUri());
                return found;
            });

            this.currentInstance = instance;
            this.baseUrl = "http://" + instance.getHost() + ":" + instance.getPort();
            log.info("Stats Service Info обновлён: baseUrl={}", baseUrl);

        } catch (Exception e) {
            log.error("Не удалось получить информацию о сервисе статистики: {}", e.getMessage());
            throw new RuntimeException(
                    "Не удалось получить информацию о сервисе статистики с id: " + statsServiceId,
                    e
            );
        }
    }

    public URI getUri(String path) {
        if (baseUrl == null) {
            log.warn("baseUrl == null, выполняем refresh");
            refreshServiceInfo();
        }
        return URI.create(baseUrl + path);
    }

    public void refresh() {
        log.info("Принудительное обновление информации о Stats Server");
        refreshServiceInfo();
    }
}