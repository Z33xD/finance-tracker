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

    public Transaction updateTransaction(Transaction updatedTransaction) {
        Optional<Transaction> existingTransaction = transactionRepository.findById(updatedTransaction.getId());

        if (existingTransaction.isPresent()) {
            Transaction transactionToUpdate = existingTransaction.get();
            transactionToUpdate.setAccount_id(updatedTransaction.getAccount_id());
            transactionToUpdate.setCategory_id(updatedTransaction.getCategory_id());
            transactionToUpdate.setAmount(updatedTransaction.getAmount());
            transactionToUpdate.setTransaction_date(updatedTransaction.getTransaction_date());
            transactionToUpdate.setDescription(updatedTransaction.getDescription());
            transactionToUpdate.setTransaction_type(updatedTransaction.getTransaction_type());
            transactionToUpdate.setImport_batch_id(updatedTransaction.getImport_batch_id());

            transactionRepository.save(transactionToUpdate);
            return transactionToUpdate;
        }
        return null;
    }

    @Transactional
    public void deleteUser(int id) {
        transactionRepository.deleteById(id);
    }
}
