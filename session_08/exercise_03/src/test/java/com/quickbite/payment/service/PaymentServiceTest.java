package com.quickbite.payment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService();
    }

    @Test
    @DisplayName("Should validate valid payment request successfully")
    void testValidatePaymentRequest_Valid() {
        boolean isValid = paymentService.validatePaymentRequest(150000.0, "VND");
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should reject payment request with negative or zero amount")
    void testValidatePaymentRequest_InvalidAmount() {
        boolean isZero = paymentService.validatePaymentRequest(0.0, "VND");
        boolean isNegative = paymentService.validatePaymentRequest(-50000.0, "VND");

        assertFalse(isZero);
        assertFalse(isNegative);
    }

    @Test
    @DisplayName("Should reject payment request with null or empty currency")
    void testValidatePaymentRequest_InvalidCurrency() {
        boolean isNullCurrency = paymentService.validatePaymentRequest(100.0, null);
        boolean isEmptyCurrency = paymentService.validatePaymentRequest(100.0, "   ");

        assertFalse(isNullCurrency);
        assertFalse(isEmptyCurrency);
    }

    @Test
    @DisplayName("Should generate valid transaction ID when processing payment")
    void testProcessPayment_Success() {
        String txnId = paymentService.processPayment(200000.0, "VND");
        assertNotNull(txnId);
        assertTrue(txnId.startsWith("TXN-"));
    }

    @Test
    @DisplayName("Should throw exception when processing invalid payment")
    void testProcessPayment_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processPayment(-10.0, "VND");
        });
    }
}
