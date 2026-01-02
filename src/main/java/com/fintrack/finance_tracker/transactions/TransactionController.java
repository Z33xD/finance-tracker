package com.fintrack.finance_tracker.transactions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(path = "/api/transactions/")
public class TransactionController {
    private final TransactionService transactionService;

    @Autowired
    public TransactionController (TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<Transaction> getTransactions(
            @RequestParam (required = false) Integer id,
            @RequestParam (required = false) LocalDate date
            ) {
        if (id != null) {
            return transactionService.getTransactionById(id)
                    .map(List::of)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        }

        else if (date != null) {
            return transactionService.getTransactionsByDate(date);
        }

        else {
            return transactionService.getTransactions();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable int id) {
        return transactionService.getTransactionById(id)
                .map(transaction -> new ResponseEntity<>(transaction, HttpStatus.OK))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Transaction> addTransaction(@RequestBody Transaction transaction) {
        Transaction createdTransaction = transactionService.addTransaction(transaction);
        return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable int id, @RequestBody Transaction transaction) {
        Transaction updatedTransaction = transactionService.updateTransaction(id, transaction);
        if (updatedTransaction != null) {
            return new ResponseEntity<>(updatedTransaction, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTransaction(@PathVariable int id) {
        transactionService.deleteUser(id);
        return new ResponseEntity<>("Transaction deleted successfully!", HttpStatus.OK);
    }

    @GetMapping("/search")
    public List<Transaction> searchTransaction (
            @RequestParam (required = false) Integer category_id,
            @RequestParam (required = false) LocalDate start_date,
            @RequestParam (required = false) LocalDate end_date,
            @RequestParam (required = false) String transaction_type
            ) {
        return transactionService.searchTransactions(category_id, start_date, end_date, transaction_type);
    }
}
