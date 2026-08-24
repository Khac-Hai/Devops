package com.example.transaction.repository;

import com.example.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionId(String transactionId);
    List<Transaction> findByFromAccountNumberOrToAccountNumberOrderByCreatedAtDesc(String fromAccountNumber, String toAccountNumber);
}
