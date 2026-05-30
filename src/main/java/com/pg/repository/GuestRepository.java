package com.pg.repository;
import com.pg.model.Guest;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
public interface GuestRepository extends MongoRepository<Guest, String> {
    List<Guest> findByStatus(Guest.GuestStatus status);
    List<Guest> findByLinkedUserId(String userId);
    List<Guest> findByBedId(String bedId);
}
