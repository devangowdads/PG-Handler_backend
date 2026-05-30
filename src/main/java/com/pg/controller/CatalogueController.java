package com.pg.controller;

import com.pg.model.Catalogue;
import com.pg.repository.CatalogueRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/catalogue")
public class CatalogueController {
    private final CatalogueRepository repo;

    public CatalogueController(CatalogueRepository repo) { this.repo = repo; }

    @GetMapping
    public ResponseEntity<List<Catalogue>> getAll() { return ResponseEntity.ok(repo.findAll()); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody Catalogue c) {
        c.setCreatedAt(LocalDateTime.now()); c.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(repo.save(c));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Catalogue req) {
        Catalogue c = repo.findById(id).orElseThrow();
        c.setName(req.getName()); c.setType(req.getType()); c.setMonthlyRent(req.getMonthlyRent());
        c.setDailyRate(req.getDailyRate()); c.setDeposit(req.getDeposit());
        c.setAmenities(req.getAmenities()); c.setDescription(req.getDescription());
        c.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(repo.save(c));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable String id) {
        repo.deleteById(id);
        return ResponseEntity.ok(Map.of("message","Deleted"));
    }
}
