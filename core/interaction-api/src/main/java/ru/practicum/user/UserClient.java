package ru.practicum.user;

import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.user.dto.UserDto;

@FeignClient(name = "user-service", path = "/admin/users")
public interface UserClient {

    @GetMapping("/{userId}")
    UserDto findUserById(
            @Positive @PathVariable Long userId
    );

}
