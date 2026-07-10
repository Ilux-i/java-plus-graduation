package ru.practicum.ewm.config;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.ApplicationInfoManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaAutoServiceRegistration;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EurekaPortUpdater {

    private final EurekaAutoServiceRegistration eurekaAutoServiceRegistration;
    private final ApplicationInfoManager applicationInfoManager;

    private volatile boolean updated = false;

    @EventListener(WebServerInitializedEvent.class)
    @Order(Ordered.LOWEST_PRECEDENCE)
    public void onApplicationEvent(WebServerInitializedEvent event) {
        if (updated) {
            return;
        }

        int actualPort = event.getWebServer().getPort();
        log.info("Tomcat started on port: {}", actualPort);

        if (actualPort <= 0) {
            log.warn("Invalid port: {}, skipping Eureka update", actualPort);
            return;
        }

        try {
            InstanceInfo currentInfo = applicationInfoManager.getInfo();

            if (currentInfo == null) {
                log.warn("InstanceInfo is null, skipping update");
                return;
            }

            if (currentInfo.getPort() == actualPort) {
                log.info("Eureka port is already correct: {}", actualPort);
                updated = true;
                return;
            }

            log.info("🔄 Updating Eureka port from {} to {}", currentInfo.getPort(), actualPort);

            // Получаем текущие метаданные как Map
            Map<String, String> metadata = new HashMap<>();
            Object currentMetadata = currentInfo.getMetadata();
            if (currentMetadata instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, String> tempMap = (Map<String, String>) currentMetadata;
                metadata.putAll(tempMap);
            }

            // Создаём новый InstanceInfo с правильным портом
            InstanceInfo updatedInfo = InstanceInfo.Builder.newBuilder()
                    .setAppName(currentInfo.getAppName())
                    .setHostName(currentInfo.getHostName())
                    .setIPAddr(currentInfo.getIPAddr())
                    .setPort(actualPort)
                    .setStatus(currentInfo.getStatus())
                    .setDataCenterInfo(currentInfo.getDataCenterInfo())
                    .setLeaseInfo(currentInfo.getLeaseInfo())
                    .setMetadata(metadata)  // Map<String, String>
                    .build();

            // Обновляем ApplicationInfoManager
            applicationInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.STARTING);

            // ✅ registerAppMetadata ожидает Map<String, String>
            applicationInfoManager.registerAppMetadata(metadata);

//            // Обновляем InstanceInfo напрямую
//            applicationInfoManager.registerAppMetadata(updatedInfo);

            applicationInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.UP);

            // Перезапускаем регистрацию
            eurekaAutoServiceRegistration.stop();
            eurekaAutoServiceRegistration.start();

            updated = true;
            log.info("✅ Successfully updated Eureka registration port to: {}", actualPort);

        } catch (Exception e) {
            log.error("Failed to update Eureka port", e);
        }
    }
}