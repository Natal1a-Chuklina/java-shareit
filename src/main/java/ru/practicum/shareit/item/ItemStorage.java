package ru.practicum.shareit.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Transactional(readOnly = true)
public interface ItemStorage extends JpaRepository<Item, Long> {
    Page<Item> findByUser_Id(Long userId, Pageable pageable);

    Page<Item> findByDescriptionContainingAndAvailableTrueOrNameContainingAndAvailableTrueAllIgnoreCase(
            String descriptionSearch, String nameSearch, Pageable pageable);

    boolean existsByUser_IdAndId(Long userId, Long itemId);

    List<Item> findByItemRequest_Id(Long id);


}
