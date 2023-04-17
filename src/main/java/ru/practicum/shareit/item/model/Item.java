package ru.practicum.shareit.item.model;

import lombok.*;
import org.apache.coyote.Request;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    private long id;
    private String name;
    private String description;
    private Boolean available;
    private long owner;
    private Request request;

    public Item(long id, String name, String description, Boolean available, long owner) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
        this.owner = owner;
    }
}
