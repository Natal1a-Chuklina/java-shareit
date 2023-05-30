package ru.practicum.shareit.item.model;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CommentMapper {
    private CommentMapper() {
    }

    public static Comment toComment(CommentDto commentDto, Item item, User user) {
        return new Comment(
                commentDto.getId(),
                commentDto.getText(),
                item,
                user,
                LocalDateTime.now()
        );
    }

    public static CommentDto toCommentDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated()
        );
    }

    public static List<CommentDto> toCommentDto(List<Comment> comments) {
        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }
}
