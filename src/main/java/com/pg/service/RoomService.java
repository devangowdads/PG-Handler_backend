package com.pg.service;

import com.pg.model.Bed;
import com.pg.model.Room;
import com.pg.repository.BedRepository;
import com.pg.repository.GuestRepository;
import com.pg.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class RoomService {

    private final RoomRepository roomRepo;
    private final BedRepository bedRepo;
    private final GuestRepository guestRepo;

    public RoomService(RoomRepository roomRepo, BedRepository bedRepo, GuestRepository guestRepo) {
        this.roomRepo = roomRepo;
        this.bedRepo = bedRepo;
        this.guestRepo = guestRepo;
    }

    // ── Safe type helpers ─────────────────────────────────────────────────────

    /** Safely parse any Object (Number, String, null) to int. */
    private static int parseInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number) o).intValue();
        try { return Integer.parseInt(o.toString().trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    /** Safely parse any Object (Number, String, null) to double. */
    static double parseDouble(Object o) {
        if (o == null) return 0.0;
        if (o instanceof Number) return ((Number) o).doubleValue();
        try { return Double.parseDouble(o.toString().trim()); }
        catch (NumberFormatException e) { return 0.0; }
    }

    /** Safely cast any Object to String (returns "" for null / non-String). */
    private static String str(Object o) {
        if (o == null) return "";
        return o.toString();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public List<Map<String, Object>> getAvailability() {
        List<Room> rooms = roomRepo.findByActiveTrue();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Room room : rooms) {
            Map<String, Object> roomMap = new LinkedHashMap<>();
            roomMap.put("id", room.getId());
            roomMap.put("roomNumber", room.getRoomNumber());
            roomMap.put("name", room.getName());
            roomMap.put("floor", room.getFloor());
            roomMap.put("type", mapRoomType(room.getType()));
            roomMap.put("description", room.getDescription());

            List<Bed> beds = bedRepo.findByRoomId(room.getId());
            List<Map<String, Object>> bedList = new ArrayList<>();

            for (Bed bed : beds) {
                Map<String, Object> bedMap = new LinkedHashMap<>();
                bedMap.put("id", bed.getId());
                bedMap.put("bedId", bed.getBedId());
                bedMap.put("status", bed.getStatus().name());

                if (bed.getCurrentGuestId() != null) {
                    guestRepo.findById(bed.getCurrentGuestId()).ifPresent(g -> {
                        Map<String, Object> gMap = new LinkedHashMap<>();
                        gMap.put("id", g.getId());
                        gMap.put("name", g.getName());
                        gMap.put("phone", g.getPhone());
                        gMap.put("checkIn", g.getCheckIn());
                        gMap.put("checkOut", g.getCheckOut());
                        gMap.put("totalRent", g.getTotalRent());
                        gMap.put("totalPaid", g.getTotalPaid());
                        gMap.put("remainingDue", g.getRemainingDue());
                        gMap.put("advanceToReturn", g.getAdvanceToReturn());
                        bedMap.put("currentGuest", gMap);
                    });
                }
                bedList.add(bedMap);
            }
            roomMap.put("beds", bedList);
            result.add(roomMap);
        }
        return result;
    }

    public List<Map<String, Object>> getAllWithBeds() {
        List<Room> rooms = roomRepo.findByActiveTrue();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Room room : rooms) {
            Map<String, Object> m = roomToMap(room);
            m.put("beds", bedRepo.findByRoomId(room.getId()));
            result.add(m);
        }
        return result;
    }

    public Room create(Map<String, Object> req) {
        Room room = new Room();
        room.setRoomNumber(str(req.get("roomNumber")));
        room.setName(str(req.get("name")));
        room.setFloor(parseInt(req.get("floor")));
        room.setType(parseRoomType(str(req.get("type"))));
        room.setDescription(str(req.get("description")));
        room.setActive(true);
        room.setCreatedAt(LocalDateTime.now());
        room.setUpdatedAt(LocalDateTime.now());
        return roomRepo.save(room);
    }

    public Room update(String id, Map<String, Object> req) {
        Room room = roomRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found: " + id));
        if (req.containsKey("roomNumber")) room.setRoomNumber(str(req.get("roomNumber")));
        if (req.containsKey("name"))       room.setName(str(req.get("name")));
        if (req.containsKey("floor"))      room.setFloor(parseInt(req.get("floor")));
        if (req.containsKey("type"))       room.setType(parseRoomType(str(req.get("type"))));
        if (req.containsKey("description")) room.setDescription(str(req.get("description")));
        room.setUpdatedAt(LocalDateTime.now());
        return roomRepo.save(room);
    }

    public void delete(String id) {
        Room room = roomRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found: " + id));
        room.setActive(false);
        room.setUpdatedAt(LocalDateTime.now());
        roomRepo.save(room);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Map<String, Object> roomToMap(Room r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("roomNumber", r.getRoomNumber());
        m.put("name", r.getName());
        m.put("floor", r.getFloor());
        m.put("type", mapRoomType(r.getType()));
        m.put("description", r.getDescription());
        return m;
    }

    public static String mapRoomType(Room.RoomType t) {
        if (t == null) return "2_SHARING";
        return switch (t) {
            case SINGLE        -> "SINGLE";
            case TWO_SHARING   -> "2_SHARING";
            case THREE_SHARING -> "3_SHARING";
            case FOUR_SHARING  -> "4_SHARING";
        };
    }

    public static Room.RoomType parseRoomType(String s) {
        if (s == null || s.isBlank()) return Room.RoomType.TWO_SHARING;
        return switch (s.trim().toUpperCase()) {
            case "SINGLE"    -> Room.RoomType.SINGLE;
            case "2_SHARING" -> Room.RoomType.TWO_SHARING;
            case "3_SHARING" -> Room.RoomType.THREE_SHARING;
            case "4_SHARING" -> Room.RoomType.FOUR_SHARING;
            default          -> Room.RoomType.TWO_SHARING;
        };
    }
}
