package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.event.EventListener;

import java.util.TimeZone;

@SpringBootApplication(scanBasePackages = {"ru.practicum", "ru.practicum"})
@EnableFeignClients
@EnableDiscoveryClient
public class EventApp {
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public static void main(String[] args) {
        SpringApplication.run(EventApp.class, args);
    }
}