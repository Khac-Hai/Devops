package com.example.transaction.service;

import com.example.transaction.client.AccountServiceClient;
import com.example.transaction.dto.AccountDto;
import com.example.transaction.dto.TransferRequest;
import com.example.transaction.dto.TransferResponse;
import com.example.transaction.entity.Transaction;
import com.example.transaction.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final AccountServiceClient accountServiceClient;
    private final TransactionRepository transactionRepository;

    public TransactionService(AccountServiceClient accountServiceClient,
                              TransactionRepository transactionRepository) {
        this.accountServiceClient = accountServiceClient;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransferResponse processTransfer(TransferRequest request) {
        String txId = "TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("Processing transfer {} from {} to {} of amount {}",
                txId, request.getFromAccountNumber(), request.getToAccountNumber(), request.getAmount());

        // 1. Basic Validation
        if (request.getFromAccountNumber() == null || request.getFromAccountNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Source account number is required");
        }
        if (request.getToAccountNumber() == null || request.getToAccountNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Destination account number is required");
        }
        if (request.getFromAccountNumber().trim().equalsIgnoreCase(request.getToAccountNumber().trim())) {
            throw new IllegalArgumentException("Source and destination accounts must be different");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }

        String fromAcc = request.getFromAccountNumber().trim();
        String toAcc = request.getToAccountNumber().trim();
        BigDecimal amount = request.getAmount();

        // 2. Inter-Service Call: Verify Source Account
        log.info("[Step 1] Verifying source account {} with account-service...", fromAcc);
        Optional<AccountDto> sourceAccountOpt = accountServiceClient.getAccountByNumber(fromAcc);
        if (sourceAccountOpt.isEmpty()) {
            recordFailedTransaction(txId, fromAcc, toAcc, amount, "VND", request.getDescription(), "Source account does not exist in account-service");
            throw new IllegalArgumentException("Source account '" + fromAcc + "' does not exist in account-service");
        }

        AccountDto sourceAccount = sourceAccountOpt.get();
        if (!"ACTIVE".equalsIgnoreCase(sourceAccount.getStatus())) {
            recordFailedTransaction(txId, fromAcc, toAcc, amount, sourceAccount.getCurrency(), request.getDescription(), "Source account is not active");
            throw new IllegalArgumentException("Source account '" + fromAcc + "' is not active (status: " + sourceAccount.getStatus() + ")");
        }

        // 3. Check Balance
        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            recordFailedTransaction(txId, fromAcc, toAcc, amount, sourceAccount.getCurrency(), request.getDescription(),
                    "Insufficient balance. Current balance: " + sourceAccount.getBalance() + ", Requested: " + amount);
            throw new IllegalArgumentException("Insufficient balance in source account '" + fromAcc + "'. Current balance: " + sourceAccount.getBalance() + ", Requested: " + amount);
        }

        // 4. Inter-Service Call: Verify Destination Account
        log.info("[Step 2] Verifying destination account {} with account-service...", toAcc);
        Optional<AccountDto> destAccountOpt = accountServiceClient.getAccountByNumber(toAcc);
        if (destAccountOpt.isEmpty()) {
            recordFailedTransaction(txId, fromAcc, toAcc, amount, sourceAccount.getCurrency(), request.getDescription(), "Destination account does not exist in account-service");
            throw new IllegalArgumentException("Destination account '" + toAcc + "' does not exist in account-service");
        }

        AccountDto destAccount = destAccountOpt.get();
        if (!"ACTIVE".equalsIgnoreCase(destAccount.getStatus())) {
            recordFailedTransaction(txId, fromAcc, toAcc, amount, destAccount.getCurrency(), request.getDescription(), "Destination account is not active");
            throw new IllegalArgumentException("Destination account '" + toAcc + "' is not active (status: " + destAccount.getStatus() + ")");
        }

        // 5. Execute Money Transfer (Debit & Credit)
        log.info("[Step 3] Executing debit and credit on account-service...");
        boolean debited = accountServiceClient.adjustBalance(fromAcc, amount.negate());
        if (!debited) {
            recordFailedTransaction(txId, fromAcc, toAcc, amount, sourceAccount.getCurrency(), request.getDescription(), "Failed to debit source account");
            throw new IllegalStateException("Failed to debit source account during transfer");
        }

        boolean credited = accountServiceClient.adjustBalance(toAcc, amount);
        if (!credited) {
            // Rollback debit
            accountServiceClient.adjustBalance(fromAcc, amount);
            recordFailedTransaction(txId, fromAcc, toAcc, amount, sourceAccount.getCurrency(), request.getDescription(), "Failed to credit destination account");
            throw new IllegalStateException("Failed to credit destination account during transfer");
        }

        // 6. Save Successful Transaction Record
        Transaction tx = new Transaction(
                txId,
                fromAcc,
                toAcc,
                amount,
                sourceAccount.getCurrency(),
                request.getDescription(),
                "SUCCESS",
                null
        );
        transactionRepository.save(tx);
        log.info("Transfer {} completed successfully!", txId);

        return new TransferResponse(
                txId,
                fromAcc,
                toAcc,
                amount,
                sourceAccount.getCurrency(),
                request.getDescription(),
                "SUCCESS",
                LocalDateTime.now()
        );
    }

    private void recordFailedTransaction(String txId, String from, String to, BigDecimal amount, String currency, String desc, String reason) {
        try {
            Transaction tx = new Transaction(txId, from, to, amount, currency, desc, "FAILED", reason);
            transactionRepository.save(tx);
        } catch (Exception e) {
            log.error("Could not record failed transaction {}: {}", txId, e.getMessage());
        }
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Optional<Transaction> getTransactionById(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId);
    }

    public List<Transaction> getTransactionsByAccount(String accountNumber) {
        return transactionRepository.findByFromAccountNumberOrToAccountNumberOrderByCreatedAtDesc(accountNumber, accountNumber);
    }
}
