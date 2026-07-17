package ru.practicum.user;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "user-service", path = "/admin/users")
public interface UserClient {

}
