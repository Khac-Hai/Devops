package com.example.account.controller;

import com.example.account.dto.AccountRequest;
import com.example.account.dto.ApiResponse;
import com.example.account.entity.Account;
import com.example.account.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Account>> createAccount(@RequestBody AccountRequest request) {
        try {
            Account created = accountService.createAccount(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Account created successfully", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create account: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Account>>> getAllAccounts() {
        List<Account> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(ApiResponse.success("Accounts retrieved successfully", accounts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Account>> getAccountById(@PathVariable Long id) {
        return accountService.getAccountById(id)
                .map(account -> ResponseEntity.ok(ApiResponse.success("Account found", account)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Account not found with ID: " + id)));
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<ApiResponse<Account>> getAccountByNumber(@PathVariable String accountNumber) {
        return accountService.getAccountByNumber(accountNumber)
                .map(account -> ResponseEntity.ok(ApiResponse.success("Account found", account)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Account not found with account number: " + accountNumber)));
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAccountBalance(@PathVariable Long id) {
        return accountService.getAccountById(id)
                .map(account -> {
                    Map<String, Object> balanceData = new HashMap<>();
                    balanceData.put("accountId", account.getId());
                    balanceData.put("accountNumber", account.getAccountNumber());
                    balanceData.put("accountHolderName", account.getAccountHolderName());
                    balanceData.put("balance", account.getBalance());
                    balanceData.put("currency", account.getCurrency());
                    return ResponseEntity.ok(ApiResponse.success("Account balance retrieved", balanceData));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Account not found with ID: " + id)));
    }

    @PutMapping("/{accountNumber}/adjust-balance")
    public ResponseEntity<ApiResponse<Account>> adjustBalance(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amountDelta) {
        try {
            Account updated = accountService.updateBalance(accountNumber, amountDelta);
            return ResponseEntity.ok(ApiResponse.success("Balance updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update balance: " + e.getMessage()));
        }
    }
}
