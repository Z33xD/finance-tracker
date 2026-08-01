package com.fintrack.finance_tracker.transactions;

import com.fintrack.finance_tracker.accounts.Account;
import com.fintrack.finance_tracker.accounts.AccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    public List<Transaction> getTransactions() {
        return transactionRepository.findAll();
    }

    public Optional<Transaction> getTransactionById(int searchKey) {
        return transactionRepository.findById(searchKey);
    }

    public List<Transaction> getTransactionsByDate(LocalDate searchKey) {
        return transactionRepository.findAll().stream()
                .filter(transaction -> (transaction.getTransactionDate().equals(searchKey)))
                .collect(Collectors.toList());
    }

    public List<Transaction> getTransactionsForUser(int userId, LocalDate date) {
        List<Account> accounts = accountRepository.findByUserId(userId);

        List<Integer> accountIds = accounts.stream()
                .map(Account::getId)
                .toList();

        if (date != null) {
            return transactionRepository.findByAccountIdInAndTransactionDate(accountIds, date);
        }

        return transactionRepository.findByAccountIdIn(accountIds);
    }

    public boolean isTransactionOwnedByUser(int transactionId, int userId) {
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
        Account account = accountRepository.findById(tx.getAccount_id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        return account.getUserId() != userId;
    }

    public boolean isAccountOwnedByUser(int accountId, int userId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        return account.getUserId() == userId;
    }

    @Transactional
    public Transaction addTransaction(Transaction transaction) {
        if (transaction.getDatetime() == null) {
            transaction.setDatetime(LocalDateTime.now());
        }
        if (transaction.getTransactionDate() == null) {
            transaction.setTransactionDate(LocalDate.now());
        }
        transactionRepository.save(transaction);
        applyBalanceChange(transaction.getAccount_id(), transaction.getAmount(), transaction.getTransactionType(), true);
        return transaction;
    }

    @Transactional
    public Transaction updateTransaction(int id, Transaction updatedTransaction) {
        Optional<Transaction> existingTransaction = transactionRepository.findById(id);

        if (existingTransaction.isPresent()) {
            Transaction transactionToUpdate = existingTransaction.get();

            applyBalanceChange(transactionToUpdate.getAccount_id(), transactionToUpdate.getAmount(), transactionToUpdate.getTransactionType(), false);

            transactionToUpdate.setAccountId(updatedTransaction.getAccount_id());

            transactionToUpdate.setCategoryId(updatedTransaction.getCategoryId());

            transactionToUpdate.setAmount(updatedTransaction.getAmount());

            if (updatedTransaction.getTransactionDate() != null) {
                transactionToUpdate.setTransactionDate(updatedTransaction.getTransactionDate());
            }

            if (updatedTransaction.getDescription() != null) {
                transactionToUpdate.setDescription(updatedTransaction.getDescription());
            }

            if (updatedTransaction.getTransactionType() != null) {
                transactionToUpdate.setTransactionType(updatedTransaction.getTransactionType());
            }

            transactionToUpdate.setImportBatchId(updatedTransaction.getImportBatchId());

            transactionRepository.save(transactionToUpdate);
            applyBalanceChange(transactionToUpdate.getAccount_id(), transactionToUpdate.getAmount(), transactionToUpdate.getTransactionType(), true);
            return transactionToUpdate;
        }
        return null;
    }

    public List<Transaction> searchTransactionsForUser(Integer userId, Integer category_id, LocalDate start_date, LocalDate end_date, String transaction_type) {
        List<Integer> userAccountIds = accountRepository.findByUserId(userId)
                .stream()
                .map(Account::getId)
                .toList();

        List<Transaction> transactions = transactionRepository.findAll();

        transactions = transactions.stream()
                .filter(transaction -> userAccountIds.contains(transaction.getAccount_id()))
                .collect(Collectors.toList());

        if (category_id != null) {
            transactions = transactions.stream()
                    .filter(transaction -> Objects.equals(transaction.getCategoryId(), category_id))
                    .collect(Collectors.toList());
        }

        if (transaction_type != null) {
            transactions = transactions.stream()
                    .filter(transaction -> transaction.getTransactionType().equalsIgnoreCase(transaction_type))
                    .collect(Collectors.toList());
        }

        if ((start_date != null) && (end_date != null)) {
            transactions = transactions.stream()
                    .filter(transaction -> ((!transaction.getTransactionDate().isBefore(start_date)) && (!transaction.getTransactionDate().isAfter(end_date))))
                    .collect(Collectors.toList());
        }

        else if (start_date != null) {
            transactions = transactions.stream()
                    .filter(transaction -> !transaction.getTransactionDate().isBefore(start_date))
                    .collect(Collectors.toList());
        }

        else if (end_date != null) {
            transactions = transactions.stream()
                    .filter(transaction -> !transaction.getTransactionDate().isAfter(end_date))
                    .collect(Collectors.toList());
        }

        return transactions;
    }

    @Transactional
    public void deleteTransaction(int id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));

        applyBalanceChange(transaction.getAccount_id(), transaction.getAmount(), transaction.getTransactionType(), false);
        transactionRepository.deleteById(id);
    }

    private void applyBalanceChange(int accountId, double amount, String transactionType, boolean isAddition) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        double delta = isIncome(transactionType) ? amount : -amount;
        if (!isAddition) {
            delta = -delta;
        }

        double currentBalance = (account.getBalance() == null) ? account.getInitialBalance() : account.getBalance();
        account.setBalance(currentBalance + delta);
        accountRepository.save(account);
    }

    private boolean isIncome(String transactionType) {
        return transactionType != null
                && (transactionType.equalsIgnoreCase("INCOME") || transactionType.equalsIgnoreCase("CREDIT"));
    }
}
