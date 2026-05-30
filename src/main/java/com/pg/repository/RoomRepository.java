package com.pg.repository;
import com.pg.model.Room;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
public interface RoomRepository extends MongoRepository<Room, String> {
    List<Room> findByActiveTrue();
}
