package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.Comment;
import ru.practicum.comment.CommentMapper;
import ru.practicum.comment.CommentStatus;
import ru.practicum.comment.dto.CommentResponseDto;
import ru.practicum.comment.dto.CommentStatusUpdateRequest;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentUserRequest;
import ru.practicum.constants.Constants;
import ru.practicum.event.client.EventClient;
import ru.practicum.event.model.Event;
import ru.practicum.exception.CommentException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.repository.CommentRepository;
import ru.practicum.user.User;
import ru.practicum.user.UserClient;
import ru.practicum.user.UserMapper;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserClient userClient;
    private final EventClient eventClient;

    @Transactional
    @Override
    public CommentResponseDto addComment(Long userId, Long eventId, NewCommentDto dto) {
        User author = UserMapper.toEntity(userClient.findUserById(userId));

        Event event = eventClient.findById(eventId);
        Comment newComment = CommentMapper.dtoToComment(
                dto,
                CommentStatus.PENDING,
                LocalDateTime.now(),
                LocalDateTime.now(),
                author,
                event
        );

        Comment addedComment = commentRepository.save(newComment);
        log.info("Создан новый комментарий с ID: {}.", addedComment.getId());
        return CommentMapper.commentToResponseDto(addedComment);
    }

    @Transactional
    @Override
    public CommentResponseDto patchCommentById(UpdateCommentUserRequest dto) {
        Comment oldComment = commentRepository.findById(dto.getId()).orElseThrow(() -> new NotFoundException(
                "Обновление комментария. Комментарий с ID: " + dto.getId() + " не найден."));

        if (!oldComment.getAuthor().getId().equals(dto.getUserId())) {
            log.error("Обновление комментария. Переданное ID пользователя не совпадает с ID автора комментария.");
            throw new ValidationException("Обновление комментария. " +
                    "Переданное ID пользователя не совпадает с ID автора комментария.");
        } else if (!userClient.existsByUserId(dto.getUserId())) {
            log.error("Обновление комментария. Пользователь с ID: {} не найден.", dto.getUserId());
            throw new NotFoundException("Обновление комментария. " +
                    "Пользователь с ID: " + dto.getUserId() + " не найден.");
        }

        if (!oldComment.getEvent().getId().equals(dto.getEventId())) {
            log.error("Обновление комментария. Переданное ID события не совпадает с ID события комментария.");
            throw new ValidationException("Обновление комментария. " +
                    "Переданное ID события не совпадает с ID события комментария.");
        } else if (!eventClient.existsById(dto.getEventId())) {
            log.error("Обновление комментария. Событие с ID: {} не найдено.", dto.getEventId());
            throw new NotFoundException("Обновление комментария. Событие с ID: " + dto.getEventId() + " не найдено.");
        }

        LocalDateTime createdAt = oldComment.getCreatedAt();
        LocalDateTime now = LocalDateTime.now();
        if ((oldComment.getStatus().equals(CommentStatus.APPROVED)
                || oldComment.getStatus().equals(CommentStatus.PENDING))
                && createdAt.isBefore(now.minusHours(24L))) {
            log.error("Обновление комментария. C момента публикации комментария с ID: {} прошло более 24 часов. " +
                    "Создание: {}. Попытка изменения: {}. Редактирование невозможно.",
                    oldComment.getId(), createdAt.format(Constants.FORMATTER), now.format(Constants.FORMATTER));
            throw new ValidationException("Обновление комментария. " +
                    "C момента публикации комментария прошло более 24 часов. Редактирование невозможно.");
        }

        oldComment.setContent(dto.getContent());
        oldComment.setUpdatedAt(LocalDateTime.now());

        Comment patchedComment = commentRepository.save(oldComment);
        log.info("Данные комментария с ID: {} обновлены.", oldComment.getId());
        return CommentMapper.commentToResponseDto(patchedComment);
    }

    @Transactional
    @Override
    public void removeCommentById(Long userId, Long eventId, Long commentId) {
        Comment removedComment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException(
                "Удаление комментария. Комментарий с ID: " + commentId + " не найден."));

        if (!removedComment.getAuthor().getId().equals(userId)) {
            log.error("Удаление комментария. Переданное ID пользователя не совпадает с ID автора комментария.");
            throw new ValidationException("Удаление комментария. " +
                    "Переданное ID пользователя не совпадает с ID автора комментария.");
        } else if (!userClient.existsByUserId(userId)) {
            log.error("Удаление комментария. Пользователь с ID: {} не найден.", userId);
            throw new NotFoundException("Удаление комментария. " +
                    "Пользователь с ID: " + userId + " не найден.");
        }

        if (!removedComment.getEvent().getId().equals(eventId)) {
            log.error("Удаление комментария. Переданное ID события не совпадает с ID события комментария.");
            throw new ValidationException("Удаление комментария. " +
                    "Переданное ID события не совпадает с ID события комментария.");
        } else if (!eventClient.existsById(eventId)) {
            log.error("Удаление комментария. Событие с ID: {} не найдено.", eventId);
            throw new NotFoundException("Удаление комментария. Событие с ID: " + eventId + " не найдено.");
        }

        commentRepository.deleteById(commentId);
        log.info("Комментарий с ID: {} удален.", commentId);
    }

    @Override
    public Page<CommentResponseDto> getApprovedCommentsByEvent(Long eventId, Pageable pageable) {
        log.debug("Получение подтвержденных комментариев для event: {}", eventId);

        Page<Comment> comments = commentRepository.findByEventIdAndStatus(
                eventId, CommentStatus.APPROVED, pageable);

        return comments.map(CommentMapper::commentToResponseDto);
    }

    @Transactional
    @Override
    public CommentResponseDto updateCommentStatus(Long commentId, CommentStatusUpdateRequest request) {

        log.info("Admin: изменить статус комментария с id={} на status - {}", commentId, request.getStatus());

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(String.format("Комментарий с ID=%d не найден", commentId)));

        CommentStatus newStatus;
        try {
            newStatus = CommentStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CommentException("Недопустимый статус: " + request.getStatus());
        }

        if (comment.getStatus() == newStatus) {
            throw new CommentException(String.format(
                    "Комментарий с ID=%d уже имеет статус '%s'", commentId, newStatus));
        }

        comment.setStatus(newStatus);
        comment.setUpdatedAt(LocalDateTime.now());

        Comment updated = commentRepository.save(comment);
        log.info("Admin: статус комментария с id={} изменен на {}", commentId, newStatus);

        return CommentMapper.commentToResponseDto(updated);
    }

    @Override
    public Page<CommentResponseDto> getCommentsByEvent(Long eventId, String status, Pageable pageable) {
        log.debug("Admin: получить комментарии по событию eventId= {}, status: {}", eventId, status);

        if (!eventClient.existsById(eventId)) {
            throw new NotFoundException("Событие с ID: " + eventId + " не найдено");
        }

        if (status != null && !status.isBlank()) {
            try {
                CommentStatus commentStatus = CommentStatus.valueOf(status.toUpperCase());
                return commentRepository.findByEventIdAndStatus(eventId, commentStatus, pageable)
                        .map(CommentMapper::commentToResponseDto);
            } catch (IllegalArgumentException e) {
                log.warn("Некорректный статус: {}", status);
                throw new ValidationException("Некорректный статус. Допустимые значения: PENDING, APPROVED, REJECTED");
            }
        }
        // Без статуса - возвращаем все комментарии события
        return commentRepository.findByEventId(eventId, pageable)
                .map(CommentMapper::commentToResponseDto);
    }

    @Override
    @Transactional
    public void deleteAdminComment(Long commentId) {
        log.info("Admin: удалить комментарий с id: {}", commentId);

        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Комментарий с ID: " + commentId + " не найден");
        }

        commentRepository.deleteById(commentId);
        log.info("Admin: comment deleted, id: {}", commentId);
    }
}
