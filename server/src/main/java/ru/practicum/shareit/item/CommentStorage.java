package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface CommentStorage extends JpaRepository<Comment, Long> {
    boolean existsByItem_IdAndAuthor_Id(Long itemId, Long userId);

    List<Comment> findByItem_IdOrderByIdAsc(Long id);

}
