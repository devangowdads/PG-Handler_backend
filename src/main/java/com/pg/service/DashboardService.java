package com.pg.service;

import com.pg.model.Bed;
import com.pg.model.Guest;
import com.pg.model.Room;
import com.pg.repository.BedRepository;
import com.pg.repository.GuestRepository;
import com.pg.repository.RoomRepository;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class DashboardService {
    private final GuestRepository guestRepo;
    private final BedRepository bedRepo;
    private final RoomRepository roomRepo;

    public DashboardService(GuestRepository guestRepo, BedRepository bedRepo, RoomRepository roomRepo) {
        this.guestRepo = guestRepo;
        this.bedRepo = bedRepo;
        this.roomRepo = roomRepo;
    }

    public Map<String, Object> getStats() {
        List<Guest> allGuests = guestRepo.findAll();
        List<Bed> allBeds = bedRepo.findAll();
        List<Room> rooms = roomRepo.findAll();

        long activeGuests = allGuests.stream().filter(g -> g.getStatus() == Guest.GuestStatus.ACTIVE || g.getStatus() == Guest.GuestStatus.DAILY).count();
        long occupiedBeds = allBeds.stream().filter(b -> b.getStatus() == Bed.BedStatus.OCCUPIED).count();

        // Group by room type
        Map<String, long[]> typeStats = new LinkedHashMap<>();
        typeStats.put("Single", new long[]{0, 0});
        typeStats.put("2 Sharing", new long[]{0, 0});
        typeStats.put("3 Sharing", new long[]{0, 0});
        typeStats.put("4 Sharing", new long[]{0, 0});

        for (Room r : rooms) {
            if (!r.isActive()) continue;
            String key = switch (r.getType()) {
                case SINGLE -> "Single";
                case TWO_SHARING -> "2 Sharing";
                case THREE_SHARING -> "3 Sharing";
                case FOUR_SHARING -> "4 Sharing";
            };
            List<Bed> beds = bedRepo.findByRoomId(r.getId());
            long occ = beds.stream().filter(b -> b.getStatus() == Bed.BedStatus.OCCUPIED).count();
            typeStats.get(key)[0] += occ;
            typeStats.get(key)[1] += (beds.size() - occ);
        }

        List<Map<String, Object>> roomTypeStats = new ArrayList<>();
        typeStats.forEach((type, counts) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("type", type); m.put("occupied", counts[0]); m.put("available", counts[1]);
            roomTypeStats.add(m);
        });

        return Map.of(
                "totalGuests", allGuests.size(),
                "activeGuests", activeGuests,
                "totalBeds", allBeds.size(),
                "occupiedBeds", occupiedBeds,
                "roomTypeStats", roomTypeStats
        );
    }
}
