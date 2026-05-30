package com.pg.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "rooms")
public class Room {
    @Id
    private String id;
    private String roomNumber;
    private String name;
    private int floor;
    private RoomType type;
    private String description;
    private boolean active = true;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum RoomType {
        SINGLE, TWO_SHARING, THREE_SHARING, FOUR_SHARING
    }
}
