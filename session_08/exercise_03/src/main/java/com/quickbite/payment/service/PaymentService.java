package com.quickbite.payment.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentService {

    public boolean validatePaymentRequest(double amount, String currency) {
        if (amount <= 0) {
            return false;
        }
        if (currency == null || currency.trim().isEmpty()) {
            return false;
        }
        return true;
    }

    public String processPayment(double amount, String currency) {
        if (!validatePaymentRequest(amount, currency)) {
            throw new IllegalArgumentException("Invalid payment request");
        }
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
