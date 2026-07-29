package ru.practicum.comment;

import lombok.experimental.UtilityClass;
import ru.practicum.comment.dto.CommentResponseDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.event.model.Event;
import ru.practicum.user.User;

import java.time.LocalDateTime;

@UtilityClass
public class CommentMapper {

    public Comment dtoToComment(
            NewCommentDto dto,
            CommentStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            User author,
            Event event
    ) {
        return Comment.builder()
                .content(dto.getContent())
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .author(author)
                .event(event)
                .build();
    }

    public CommentResponseDto commentToResponseDto(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .eventId(comment.getEvent().getId())
                .userId(comment.getAuthor().getId())
                .authorName(comment.getAuthor().getName())
                .content(comment.getContent())
                .status(comment.getStatus().toString())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

}