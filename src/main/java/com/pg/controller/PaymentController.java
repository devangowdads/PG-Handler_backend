package com.pg.controller;

import com.pg.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) { this.paymentService = paymentService; }

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(required = false) String userId) {
        return ResponseEntity.ok(paymentService.getAll(userId));
    }

    @GetMapping("/guest/{guestId}")
    public ResponseEntity<?> getByGuest(@PathVariable String guestId) {
        return ResponseEntity.ok(paymentService.getByGuest(guestId));
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary() { return ResponseEntity.ok(paymentService.getSummary()); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> req) {
        return ResponseEntity.ok(paymentService.create(req));
    }
}
