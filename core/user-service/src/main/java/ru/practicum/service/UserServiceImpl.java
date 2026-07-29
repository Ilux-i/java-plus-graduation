package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.User;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.UserMapper;
import ru.practicum.repository.UserRepository;
import ru.practicum.user.dto.UserShortDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest request) {
        log.info("Создание пользователя: {}", request);

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Пользователь с email '" + request.getEmail() + "' уже существует");
        }
        User user = UserMapper.toEntity(request);
        User savedUser = userRepository.save(user);
        log.info("Пользователь создан с id: {}", savedUser.getId());
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        log.info("Получение пользователей: ids={}, from={}, size={}", ids, from, size);
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAllByIds(ids, pageable)
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя с id: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        userRepository.deleteById(userId);
        log.info("Пользователь с id: {} удален", userId);
    }

    @Override
    public UserDto findUserById(Long userId) {
        return UserMapper.toUserDto(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользхователя с id: " + userId + ", не существует"))
        );
    }

    @Override
    public Boolean existsByUserId(Long userId) {
        return userRepository.existsById(userId);
    }

    @Override
    public Map<Long, UserShortDto> findAllUsers(List<Long> userIds) {
        List<User> users = userRepository.findAllById(userIds);

        Map<Long, UserShortDto> res = new HashMap<>();
        for (User user : users) {
            res.put(user.getId(), UserMapper.toUserShortDto(user));
        }

        return res;
    }

}
