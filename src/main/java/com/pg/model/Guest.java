package com.pg.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "guests")
public class Guest {
    @Id
    private String id;
    private String name;
    private String phone;
    private String email;
    private String idProof;
    private String idNumber;
    private String address;
    private String emergencyContact;

    private String roomId;
    private String bedId;       // the bedId string (like "A1")
    private String bedDocId;    // MongoDB document id of the bed

    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private GuestType guestType = GuestType.MONTHLY;
    private GuestStatus status = GuestStatus.ACTIVE;

    private double monthlyRent;
    private double totalRent;
    private double advancePaid;
    private double totalPaid;
    private double remainingDue;
    private double advanceToReturn;

    private String linkedUserId;  // optionally linked to a portal user

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum GuestType { MONTHLY, DAILY }
    public enum GuestStatus { ACTIVE, DAILY, CHECKED_OUT }
}
