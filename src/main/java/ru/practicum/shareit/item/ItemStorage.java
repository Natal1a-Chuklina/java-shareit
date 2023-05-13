package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage extends JpaRepository<Item, Long> {
    List<Item> findByUser_IdOrderByIdAsc(Long userId);

    List<Item> findByDescriptionContainingAndAvailableTrueOrNameContainingAndAvailableTrueAllIgnoreCase(
            String descriptionSearch, String nameSearch);

    boolean existsByUser_IdAndId(Long userId, Long itemId);
}
