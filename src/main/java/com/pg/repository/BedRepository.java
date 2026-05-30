package com.pg.repository;
import com.pg.model.Bed;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;
public interface BedRepository extends MongoRepository<Bed, String> {
    List<Bed> findByRoomId(String roomId);
    Optional<Bed> findByBedIdAndRoomId(String bedId, String roomId);
    List<Bed> findByStatus(Bed.BedStatus status);
}
