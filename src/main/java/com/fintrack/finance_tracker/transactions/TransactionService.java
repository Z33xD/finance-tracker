package com.fintrack.finance_tracker.transactions;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TransactionService {
    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<Transaction> getTransactions() {
        return transactionRepository.findAll();
    }

    public Optional<Transaction> getTransactionById(int searchKey) {
        return transactionRepository.findById(searchKey);
    }

    public List<Transaction> getTransactionsByDate(LocalDate searchKey) {
        return transactionRepository.findAll().stream()
                .filter(transaction -> (transaction.getTransaction_date().equals(searchKey)))
                .collect(Collectors.toList());
    }

    public Transaction addTransaction(Transaction transaction) {
        transactionRepository.save(transaction);
        return transaction;
    }

    public Transaction updateTransaction(int id, Transaction updatedTransaction) {
        Optional<Transaction> existingTransaction = transactionRepository.findById(id);

        if (existingTransaction.isPresent()) {
            Transaction transactionToUpdate = existingTransaction.get();
            transactionToUpdate.setAccount_id(updatedTransaction.getAccount_id());

            transactionToUpdate.setCategory_id(updatedTransaction.getCategory_id());

            transactionToUpdate.setAmount(updatedTransaction.getAmount());

            if (updatedTransaction.getTransaction_date() != null) {
                transactionToUpdate.setTransaction_date(updatedTransaction.getTransaction_date());
            }

            if (updatedTransaction.getDescription() != null) {
                transactionToUpdate.setDescription(updatedTransaction.getDescription());
            }

            if (updatedTransaction.getTransaction_type() != null) {
                transactionToUpdate.setTransaction_type(updatedTransaction.getTransaction_type());
            }

            transactionToUpdate.setImport_batch_id(updatedTransaction.getImport_batch_id());

            transactionRepository.save(transactionToUpdate);
            return transactionToUpdate;
        }
        return null;
    }

    public List<Transaction> searchTransactions(Integer category_id, LocalDate start_date, LocalDate end_date, String transaction_type) {
        List<Transaction> transactions = transactionRepository.findAll();

        if (category_id != null) {
            transactions = transactions.stream()
                    .filter(transaction -> (transaction.getCategory_id() == category_id))
                    .collect(Collectors.toList());
        }

        if (transaction_type != null) {
            transactions = transactions.stream()
                    .filter(transaction -> transaction.getTransaction_type().equalsIgnoreCase(transaction_type))
                    .collect(Collectors.toList());
        }

        if ((start_date != null) && (end_date != null)) {
            transactions = transactions.stream()
                    .filter(transaction -> ((!transaction.getTransaction_date().isBefore(start_date)) && (!transaction.getTransaction_date().isAfter(end_date))))
                    .collect(Collectors.toList());
        }

        else if (start_date != null) {
            transactions = transactions.stream()
                    .filter(transaction -> !transaction.getTransaction_date().isBefore(start_date))
                    .collect(Collectors.toList());
        }

        else if (end_date != null) {
            transactions = transactions.stream()
                    .filter(transaction -> !transaction.getTransaction_date().isAfter(end_date))
                    .collect(Collectors.toList());
        }

        return transactions;
    }

    @Transactional
    public void deleteUser(int id) {
        transactionRepository.deleteById(id);
    }
}
