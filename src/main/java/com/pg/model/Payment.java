package com.pg.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "payments")
public class Payment {
    @Id
    private String id;
    private String guestId;
    private String guestName;
    private String bedId;
    private double amount;
    private PaymentMode mode;
    private String note;
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum PaymentMode {
        CASH, UPI, BANK_TRANSFER, ONLINE
    }
}
