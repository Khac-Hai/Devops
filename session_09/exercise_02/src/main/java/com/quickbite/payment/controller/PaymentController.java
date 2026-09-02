package com.quickbite.payment.controller;

import com.quickbite.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "payment-service",
                "message", "Payment Service is operational"
        ));
    }

    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processPayment(@RequestParam double amount, @RequestParam(defaultValue = "VND") String currency) {
        String txnId = paymentService.processPayment(amount, currency);
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "transactionId", txnId,
                "amount", amount,
                "currency", currency
        ));
    }
}
