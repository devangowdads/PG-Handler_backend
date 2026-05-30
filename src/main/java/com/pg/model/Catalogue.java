package com.pg.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "catalogue")
public class Catalogue {
    @Id
    private String id;
    private String name;
    private String type;  // SINGLE, 2_SHARING, 3_SHARING, 4_SHARING
    private double monthlyRent;
    private double dailyRate;
    private double deposit;
    private List<String> amenities;
    private String description;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
