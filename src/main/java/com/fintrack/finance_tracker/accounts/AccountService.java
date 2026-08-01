package com.fintrack.finance_tracker.accounts;

import com.fintrack.finance_tracker.transactions.Transaction;
import com.fintrack.finance_tracker.transactions.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<Account> getAccounts() {
        return accountRepository.findAll();
    }

    public Optional<Account> getAccountById(int searchKey) {
        Optional<Account> account = accountRepository.findById(searchKey);
        account.ifPresent(this::backfillBalance);
        return account;
    }

    public List<Account> getAccountsByUserId(int searchKey) {
        return accountRepository.findAll().stream()
                .filter(account -> (account.getUserId() == searchKey))
                .peek(this::backfillBalance)
                .collect(Collectors.toList());
    }

    public List<Account> getAccountsByAccountType(String searchText) {
        return accountRepository.findAll().stream()
                .filter(account -> account.getAccountType().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Transactional
    public Account addAccount(Account account) {
        if (account.getCreatedAt() == null) {
            account.setCreatedAt(LocalDateTime.now());
        }
        if (account.getBalance() == null) {
            account.setBalance(account.getInitialBalance());
        }
        return accountRepository.save(account);
    }

    @Transactional
    public Account updateAccount(int id, Account updatedAccount) {
        Optional<Account> existingAccount = accountRepository.findById(id);

        if (existingAccount.isPresent()) {
            Account accountToUpdate = existingAccount.get();

            if (updatedAccount.getAccountName() != null) {
                accountToUpdate.setAccountName(updatedAccount.getAccountName());
            }
            if (updatedAccount.getAccountType() != null) {
                accountToUpdate.setAccountType(updatedAccount.getAccountType());
            }
            if (updatedAccount.getCurrency() != null) {
                accountToUpdate.setCurrency(updatedAccount.getCurrency());
            }
            accountToUpdate.setInitialBalance(updatedAccount.getInitialBalance());

            accountRepository.save(accountToUpdate);
            return accountToUpdate;
        }
        return null;
    }

    @Transactional
    public void deleteAccount(int id) {
        accountRepository.deleteById(id);
    }

    private void backfillBalance(Account account) {
        if (account.getBalance() != null) {
            return;
        }

        double computedBalance = account.getInitialBalance();
        for (Transaction transaction : transactionRepository.findByAccountId(account.getId())) {
            computedBalance += isIncome(transaction.getTransactionType())
                    ? transaction.getAmount()
                    : -transaction.getAmount();
        }
        account.setBalance(computedBalance);
    }

    private boolean isIncome(String transactionType) {
        return transactionType != null
                && (transactionType.equalsIgnoreCase("INCOME") || transactionType.equalsIgnoreCase("CREDIT"));
    }
}
