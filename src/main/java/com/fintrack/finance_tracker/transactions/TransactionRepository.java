package com.fintrack.finance_tracker.transactions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    void deleteById(int id);
    Optional<Transaction> findById(int id);
    Optional<Transaction> findByTransactionDateAndAmountAndDescriptionAndCategoryIdAndTransactionType(
            LocalDate transaction_date,
            double amount,
            String description,
            int category_id,
            String transaction_type
    );
}
