package com.example.transaction.controller;

import com.example.transaction.dto.ApiResponse;
import com.example.transaction.dto.TransferRequest;
import com.example.transaction.dto.TransferResponse;
import com.example.transaction.entity.Transaction;
import com.example.transaction.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransferResponse>> transfer(@RequestBody TransferRequest request) {
        try {
            TransferResponse response = transactionService.processTransfer(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Transfer completed successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Transfer failed: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Transaction>>> getAllTransactions() {
        List<Transaction> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved successfully", transactions));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<Transaction>> getTransactionById(@PathVariable String transactionId) {
        return transactionService.getTransactionById(transactionId)
                .map(tx -> ResponseEntity.ok(ApiResponse.success("Transaction found", tx)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Transaction not found with ID: " + transactionId)));
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<ApiResponse<List<Transaction>>> getTransactionsByAccount(@PathVariable String accountNumber) {
        List<Transaction> transactions = transactionService.getTransactionsByAccount(accountNumber);
        return ResponseEntity.ok(ApiResponse.success("Account transactions retrieved", transactions));
    }
}
