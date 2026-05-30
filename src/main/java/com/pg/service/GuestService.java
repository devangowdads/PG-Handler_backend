package com.pg.service;

import com.pg.model.Bed;
import com.pg.model.Guest;
import com.pg.repository.BedRepository;
import com.pg.repository.GuestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class GuestService {

    private final GuestRepository guestRepo;
    private final BedRepository bedRepo;

    public GuestService(GuestRepository guestRepo, BedRepository bedRepo) {
        this.guestRepo = guestRepo;
        this.bedRepo = bedRepo;
    }

    // ── Safe type helpers ─────────────────────────────────────────────────────

    static double parseDouble(Object o) {
        if (o == null) return 0.0;
        if (o instanceof Number) return ((Number) o).doubleValue();
        try { return Double.parseDouble(o.toString().trim()); }
        catch (NumberFormatException e) { return 0.0; }
    }

    private static String str(Object o) {
        if (o == null) return null;
        String s = o.toString().trim();
        return s.isEmpty() ? null : s;
    }

    /**
     * Parses ISO datetime strings from the frontend.
     * Handles:  "2024-05-01T00:00:00.000Z"  (OffsetDateTime / Instant)
     *           "2024-05-01T00:00:00"        (LocalDateTime)
     */
    private static LocalDateTime parseDateTime(Object o) {
        if (o == null) return null;
        String s = o.toString().trim();
        if (s.isEmpty()) return null;
        // Try with timezone offset / Z suffix first
        try {
            return OffsetDateTime.parse(s).toLocalDateTime();
        } catch (DateTimeParseException ignored) {}
        // Fallback: plain LocalDateTime
        try {
            return LocalDateTime.parse(s, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException ignored) {}
        // Fallback: date only "2024-05-01"
        try {
            return LocalDateTime.parse(s + "T00:00:00", DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public List<Map<String, Object>> getAll(String userId) {
        List<Guest> guests = userId != null
                ? guestRepo.findByLinkedUserId(userId)
                : guestRepo.findAll();
        return guests.stream().map(this::guestToMap).toList();
    }

    public Guest create(Map<String, Object> req) {
        Guest g = new Guest();
        fillGuest(g, req);

        // Status
        g.setStatus(g.getGuestType() == Guest.GuestType.DAILY
                ? Guest.GuestStatus.DAILY
                : Guest.GuestStatus.ACTIVE);

        // Financials
        double totalRent = parseDouble(req.get("totalRent"));
        double advance   = parseDouble(req.get("advancePaid"));
        g.setTotalPaid(advance);
        double due = totalRent - advance;
        g.setRemainingDue(due > 0 ? due : 0);
        g.setAdvanceToReturn(due < 0 ? Math.abs(due) : 0);

        g.setCreatedAt(LocalDateTime.now());
        g.setUpdatedAt(LocalDateTime.now());
        Guest saved = guestRepo.save(g);

        // Mark bed OCCUPIED
        markBed(saved.getRoomId(), saved.getBedId(), saved.getId(), Bed.BedStatus.OCCUPIED);
        return saved;
    }

    public Guest update(String id, Map<String, Object> req) {
        Guest g = guestRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Guest not found: " + id));

        String oldBedId  = g.getBedId();
        String oldRoomId = g.getRoomId();

        fillGuest(g, req);
        g.setUpdatedAt(LocalDateTime.now());

        double totalRent = parseDouble(req.get("totalRent"));
        double advance   = parseDouble(req.get("advancePaid"));
        g.setTotalPaid(advance);
        double due = totalRent - advance;
        g.setRemainingDue(due > 0 ? due : 0);
        g.setAdvanceToReturn(due < 0 ? Math.abs(due) : 0);

        // Free old bed if changed
        boolean bedChanged = oldBedId != null && !oldBedId.equals(g.getBedId());
        if (bedChanged) {
            markBed(oldRoomId, oldBedId, null, Bed.BedStatus.AVAILABLE);
        }

        // Occupy new bed
        if (g.getBedId() != null && g.getRoomId() != null) {
            markBed(g.getRoomId(), g.getBedId(), g.getId(), Bed.BedStatus.OCCUPIED);
        }

        return guestRepo.save(g);
    }

    public Guest checkout(String id) {
        Guest g = guestRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Guest not found: " + id));
        g.setStatus(Guest.GuestStatus.CHECKED_OUT);
        g.setCheckOut(LocalDateTime.now());
        markBed(g.getRoomId(), g.getBedId(), null, Bed.BedStatus.AVAILABLE);
        return guestRepo.save(g);
    }

    public void delete(String id) {
        Guest g = guestRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Guest not found: " + id));
        bedRepo.findByBedIdAndRoomId(g.getBedId(), g.getRoomId()).ifPresent(bed -> {
            if (id.equals(bed.getCurrentGuestId())) {
                bed.setStatus(Bed.BedStatus.AVAILABLE);
                bed.setCurrentGuestId(null);
                bed.setUpdatedAt(LocalDateTime.now());
                bedRepo.save(bed);
            }
        });
        guestRepo.deleteById(id);
    }

    public void addPayment(String guestId, double amount) {
        Guest g = guestRepo.findById(guestId)
                .orElseThrow(() -> new RuntimeException("Guest not found: " + guestId));
        double newPaid = g.getTotalPaid() + amount;
        g.setTotalPaid(newPaid);
        double due = g.getTotalRent() - newPaid;
        g.setRemainingDue(due > 0 ? due : 0);
        g.setAdvanceToReturn(due < 0 ? Math.abs(due) : 0);
        g.setUpdatedAt(LocalDateTime.now());
        guestRepo.save(g);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void markBed(String roomId, String bedId, String guestId, Bed.BedStatus status) {
        if (roomId == null || bedId == null) return;
        bedRepo.findByBedIdAndRoomId(bedId, roomId).ifPresent(bed -> {
            bed.setStatus(status);
            bed.setCurrentGuestId(guestId);
            bed.setUpdatedAt(LocalDateTime.now());
            bedRepo.save(bed);
        });
    }

    private void fillGuest(Guest g, Map<String, Object> req) {
        if (req.containsKey("name"))             g.setName(str(req.get("name")));
        if (req.containsKey("phone"))            g.setPhone(str(req.get("phone")));
        if (req.containsKey("email"))            g.setEmail(str(req.get("email")));
        if (req.containsKey("idProof"))          g.setIdProof(str(req.get("idProof")));
        if (req.containsKey("idNumber"))         g.setIdNumber(str(req.get("idNumber")));
        if (req.containsKey("address"))          g.setAddress(str(req.get("address")));
        if (req.containsKey("emergencyContact")) g.setEmergencyContact(str(req.get("emergencyContact")));
        if (req.containsKey("roomId"))           g.setRoomId(str(req.get("roomId")));
        if (req.containsKey("bedId"))            g.setBedId(str(req.get("bedId")));
        if (req.containsKey("linkedUserId"))     g.setLinkedUserId(str(req.get("linkedUserId")));

        if (req.containsKey("checkIn"))  g.setCheckIn(parseDateTime(req.get("checkIn")));
        if (req.containsKey("checkOut") && req.get("checkOut") != null)
                                          g.setCheckOut(parseDateTime(req.get("checkOut")));

        if (req.containsKey("guestType"))
            g.setGuestType("DAILY".equalsIgnoreCase(str(req.get("guestType")))
                    ? Guest.GuestType.DAILY : Guest.GuestType.MONTHLY);

        if (req.containsKey("monthlyRent")) g.setMonthlyRent(parseDouble(req.get("monthlyRent")));
        if (req.containsKey("totalRent"))   g.setTotalRent(parseDouble(req.get("totalRent")));
        if (req.containsKey("advancePaid")) g.setAdvancePaid(parseDouble(req.get("advancePaid")));
    }

    public Map<String, Object> guestToMap(Guest g) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", g.getId());
        m.put("name", g.getName());
        m.put("phone", g.getPhone());
        m.put("email", g.getEmail());
        m.put("idProof", g.getIdProof());
        m.put("idNumber", g.getIdNumber());
        m.put("address", g.getAddress());
        m.put("emergencyContact", g.getEmergencyContact());
        m.put("roomId", g.getRoomId());
        m.put("bedId", g.getBedId());
        m.put("checkIn", g.getCheckIn());
        m.put("checkOut", g.getCheckOut());
        m.put("guestType", g.getGuestType());
        m.put("status", g.getStatus());
        m.put("monthlyRent", g.getMonthlyRent());
        m.put("totalRent", g.getTotalRent());
        m.put("advancePaid", g.getAdvancePaid());
        m.put("totalPaid", g.getTotalPaid());
        m.put("remainingDue", g.getRemainingDue());
        m.put("advanceToReturn", g.getAdvanceToReturn());
        m.put("linkedUserId", g.getLinkedUserId());
        return m;
    }
}
