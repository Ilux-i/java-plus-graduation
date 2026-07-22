package ru.practicum.user;

import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;

import java.util.List;
import java.util.Map;

@FeignClient(name = "USER-ADMINISTRATION", path = "/admin/users")
public interface UserClient {

    @GetMapping("/{userId}")
    UserDto findUserById(
            @Positive @PathVariable Long userId
    );

    @GetMapping("/{userId}/exists")
    Boolean existsByUserId(
            @Positive @PathVariable Long userId
    );

    @PostMapping("/all")
    Map<Long, UserShortDto> findAllUsers(
            @RequestBody List<Long> userIds
    );

}
