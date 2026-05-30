package com.pg.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "beds")
public class Bed {
    @Id
    private String id;
    private String bedId;       // user-facing ID like "A1", "101-1"
    private String roomId;
    private BedStatus status = BedStatus.AVAILABLE;
    private String currentGuestId;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum BedStatus {
        AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE
    }
}
