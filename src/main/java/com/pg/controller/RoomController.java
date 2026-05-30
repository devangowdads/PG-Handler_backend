package com.pg.controller;

import com.pg.service.RoomService;
import com.pg.repository.RoomRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;
    private final RoomRepository roomRepo;

    public RoomController(RoomService roomService, RoomRepository roomRepo) {
        this.roomService = roomService;
        this.roomRepo    = roomRepo;
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(roomService.getAllWithBeds());
    }

    @GetMapping("/availability")
    public ResponseEntity<?> getAvailability() {
        return ResponseEntity.ok(roomService.getAvailability());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        return roomRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> req) {
        return ResponseEntity.ok(roomService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Map<String, Object> req) {
        return ResponseEntity.ok(roomService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable String id) {
        roomService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Room deleted"));
    }
}
