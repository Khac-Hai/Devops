package com.example.account.service;

import com.example.account.dto.AccountRequest;
import com.example.account.entity.Account;
import com.example.account.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Account createAccount(AccountRequest request) {
        if (request.getAccountNumber() == null || request.getAccountNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Account number is required");
        }
        if (request.getAccountHolderName() == null || request.getAccountHolderName().trim().isEmpty()) {
            throw new IllegalArgumentException("Account holder name is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (accountRepository.existsByAccountNumber(request.getAccountNumber())) {
            throw new IllegalArgumentException("Account with number '" + request.getAccountNumber() + "' already exists");
        }

        BigDecimal balance = request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO;
        String currency = request.getCurrency() != null && !request.getCurrency().trim().isEmpty() ? request.getCurrency() : "VND";
        String status = request.getStatus() != null && !request.getStatus().trim().isEmpty() ? request.getStatus() : "ACTIVE";

        Account account = new Account(
                request.getAccountNumber().trim(),
                request.getAccountHolderName().trim(),
                request.getEmail().trim(),
                balance,
                currency,
                status
        );

        return accountRepository.save(account);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Optional<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }

    public Optional<Account> getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    @Transactional
    public Account updateBalance(String accountNumber, BigDecimal amountDelta) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account " + accountNumber + " not found"));

        BigDecimal newBalance = account.getBalance().add(amountDelta);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Insufficient funds in account " + accountNumber);
        }
        account.setBalance(newBalance);
        return accountRepository.save(account);
    }

    @Transactional
    public void deleteAccount(Long id) {
        if (!accountRepository.existsById(id)) {
            throw new IllegalArgumentException("Account with id " + id + " not found");
        }
        accountRepository.deleteById(id);
    }
}
