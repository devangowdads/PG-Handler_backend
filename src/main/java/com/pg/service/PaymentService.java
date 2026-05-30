package com.pg.service;

import com.pg.model.Guest;
import com.pg.model.Payment;
import com.pg.repository.GuestRepository;
import com.pg.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final GuestService guestService;
    private final GuestRepository guestRepo;

    public PaymentService(PaymentRepository paymentRepo,
                          GuestService guestService,
                          GuestRepository guestRepo) {
        this.paymentRepo  = paymentRepo;
        this.guestService = guestService;
        this.guestRepo    = guestRepo;
    }

    // ── Safe helpers ──────────────────────────────────────────────────────────

    private static double parseDouble(Object o) {
        if (o == null) return 0.0;
        if (o instanceof Number) return ((Number) o).doubleValue();
        try { return Double.parseDouble(o.toString().trim()); }
        catch (NumberFormatException e) { return 0.0; }
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString().trim();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public List<Map<String, Object>> getAll(String userId) {
        List<Payment> payments;
        if (userId != null && !userId.isBlank()) {
            List<String> guestIds = guestRepo.findByLinkedUserId(userId)
                    .stream().map(Guest::getId).toList();
            payments = paymentRepo.findAll().stream()
                    .filter(p -> guestIds.contains(p.getGuestId()))
                    .toList();
        } else {
            payments = paymentRepo.findAll();
        }
        // Sort newest first
        List<Payment> sorted = new ArrayList<>(payments);
        sorted.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return sorted.stream().map(this::toMap).toList();
    }

    public Payment create(Map<String, Object> req) {
        Payment p = new Payment();
        p.setGuestId(str(req.get("guestId")));
        p.setAmount(parseDouble(req.get("amount")));   // safe — no direct cast

        String modeStr = str(req.get("mode"));
        try {
            p.setMode(Payment.PaymentMode.valueOf(modeStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
            p.setMode(Payment.PaymentMode.CASH);
        }
        p.setNote(str(req.get("note")));

        // Enrich from guest
        guestRepo.findById(p.getGuestId()).ifPresent(g -> {
            p.setGuestName(g.getName());
            p.setBedId(g.getBedId());
        });

        Payment saved = paymentRepo.save(p);
        guestService.addPayment(p.getGuestId(), p.getAmount());
        return saved;
    }

    public List<Payment> getByGuest(String guestId) {
        return paymentRepo.findByGuestId(guestId);
    }

    public Map<String, Object> getSummary() {
        List<Payment> all = paymentRepo.findAll();

        double totalCollected = all.stream()
                .mapToDouble(Payment::getAmount).sum();

        double totalDue = guestRepo.findAll().stream()
                .filter(g -> g.getStatus() != Guest.GuestStatus.CHECKED_OUT)
                .mapToDouble(Guest::getRemainingDue).sum();

        double totalAdvanceToReturn = guestRepo.findAll().stream()
                .mapToDouble(Guest::getAdvanceToReturn).sum();

        // Group by payment mode
        Map<String, Double> byModeMap = new LinkedHashMap<>();
        for (Payment p : all) {
            String mode = p.getMode() != null ? p.getMode().name() : "UNKNOWN";
            byModeMap.merge(mode, p.getAmount(), Double::sum);
        }
        List<Map<String, Object>> byMode = byModeMap.entrySet().stream()
                .map(e -> Map.<String, Object>of("mode", e.getKey(), "amount", e.getValue()))
                .toList();

        return Map.of(
                "totalCollected",       totalCollected,
                "totalDue",             totalDue,
                "totalAdvanceToReturn", totalAdvanceToReturn,
                "byMode",               byMode
        );
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private Map<String, Object> toMap(Payment p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",        p.getId());
        m.put("guestId",   p.getGuestId());
        m.put("guestName", p.getGuestName());
        m.put("bedId",     p.getBedId());
        m.put("amount",    p.getAmount());
        m.put("mode",      p.getMode());
        m.put("note",      p.getNote());
        m.put("createdAt", p.getCreatedAt());
        return m;
    }
}
