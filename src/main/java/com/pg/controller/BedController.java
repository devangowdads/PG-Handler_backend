package com.pg.controller;

import com.pg.model.Bed;
import com.pg.repository.BedRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/beds")
public class BedController {

    private final BedRepository bedRepo;

    public BedController(BedRepository bedRepo) {
        this.bedRepo = bedRepo;
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString().trim();
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<Bed>> getByRoom(@PathVariable String roomId) {
        return ResponseEntity.ok(bedRepo.findByRoomId(roomId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> req) {
        Bed bed = new Bed();
        bed.setBedId(str(req.get("bedId")));
        bed.setRoomId(str(req.get("roomId")));

        String statusStr = str(req.getOrDefault("status", "AVAILABLE"));
        try {
            bed.setStatus(Bed.BedStatus.valueOf(statusStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
            bed.setStatus(Bed.BedStatus.AVAILABLE);
        }

        bed.setCreatedAt(LocalDateTime.now());
        bed.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(bedRepo.save(bed));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Map<String, Object> req) {
        Bed bed = bedRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Bed not found: " + id));

        if (req.containsKey("bedId")) bed.setBedId(str(req.get("bedId")));
        if (req.containsKey("status")) {
            try {
                bed.setStatus(Bed.BedStatus.valueOf(str(req.get("status")).toUpperCase()));
            } catch (IllegalArgumentException e) {
                // leave existing status
            }
        }
        bed.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(bedRepo.save(bed));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable String id) {
        bedRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Bed deleted"));
    }
}
