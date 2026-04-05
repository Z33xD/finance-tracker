package com.fintrack.finance_tracker.transactions;

import com.fintrack.finance_tracker.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
            @RequestParam (required = false) LocalDate date
    ) {
        User currentUser = getAuthenticatedUser();
        return transactionService.getTransactionsForUser(currentUser.getId(), date);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable int id) {
        User currentUser = getAuthenticatedUser();

        Transaction transaction = transactionService.getTransactionById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (transactionService.isTransactionOwnedByUser(id, currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(transaction);
    }

    @PostMapping
    public ResponseEntity<Transaction> addTransaction(@RequestBody Transaction transaction) {
        User currentUser = getAuthenticatedUser();

        if (transactionService.isTransactionOwnedByUser(transaction.getAccount_id(), currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Transaction createdTransaction = transactionService.addTransaction(transaction);
        return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable int id, @RequestBody Transaction transaction) {
        User currentUser = getAuthenticatedUser();

        if (transactionService.isTransactionOwnedByUser(id, currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (transactionService.isTransactionOwnedByUser(transaction.getAccount_id(), currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

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
        User currentUser = getAuthenticatedUser();

        if (transactionService.isTransactionOwnedByUser(id, currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        transactionService.deleteTransaction(id);
        return new ResponseEntity<>("Transaction deleted successfully!", HttpStatus.OK);
    }

    @GetMapping("/search")
    public List<Transaction> searchTransaction (
            @RequestParam (required = false) Integer category_id,
            @RequestParam (required = false) LocalDate start_date,
            @RequestParam (required = false) LocalDate end_date,
            @RequestParam (required = false) String transaction_type
    ) {
        User currentUser = getAuthenticatedUser();
        return transactionService.searchTransactionsForUser(currentUser.getId(), category_id, start_date, end_date, transaction_type);
    }

    // Helper method to get the authenticated user
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof User)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return (User) principal;
    }
}
