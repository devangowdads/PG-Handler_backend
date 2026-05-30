package com.pg.controller;

import com.pg.service.GuestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/guests")
public class GuestController {
    private final GuestService guestService;

    public GuestController(GuestService guestService) { this.guestService = guestService; }

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(required = false) String userId) {
        return ResponseEntity.ok(guestService.getAll(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        return ResponseEntity.ok(guestService.getAll(null).stream()
                .filter(m -> id.equals(m.get("id"))).findFirst()
                .orElseThrow(() -> new RuntimeException("Not found")));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> req) {
        return ResponseEntity.ok(guestService.guestToMap(guestService.create(req)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Map<String, Object> req) {
        return ResponseEntity.ok(guestService.guestToMap(guestService.update(id, req)));
    }

    @PostMapping("/{id}/checkout")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> checkout(@PathVariable String id) {
        return ResponseEntity.ok(guestService.guestToMap(guestService.checkout(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable String id) {
        guestService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Guest deleted"));
    }
}
