package com.fintrack.finance_tracker.accounts;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AccountService {
    private final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<Account> getAccounts() {
        return accountRepository.findAll();
    }

    public Optional<Account> getAccountById(int searchKey) {
        return accountRepository.findById(searchKey);
    }

    public List<Account> getAccountsByUserId(int searchKey) {
        return accountRepository.findAll().stream()
                .filter(account -> (account.getUser_id() == searchKey))
                .collect(Collectors.toList());
    }

    public List<Account> getAccountsByAccountType(String searchText) {
        return accountRepository.findAll().stream()
                .filter(account -> account.getAccount_type().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());
    }

    public Account addAccount(Account account) {
        accountRepository.save(account);
        if (account.getCreated_at() == null) {
            account.setCreated_at(LocalDateTime.now());
        }
        return account;
    }

    public Account updateAccount(int id, Account updatedAccount) {
        Optional<Account> existingAccount = accountRepository.findById(id);

        if (existingAccount.isPresent()) {
            Account accountToUpdate = existingAccount.get();

            if (updatedAccount.getAccount_name() != null) {
                accountToUpdate.setAccount_name(updatedAccount.getAccount_name());
            }
            if (updatedAccount.getAccount_type() != null) {
                accountToUpdate.setAccount_type(updatedAccount.getAccount_type());
            }
            if (updatedAccount.getCurrency() != null) {
                accountToUpdate.setCurrency(updatedAccount.getCurrency());
            }
            accountToUpdate.setInitial_balance(updatedAccount.getInitial_balance());

            accountRepository.save(accountToUpdate);
            return accountToUpdate;
        }
        return null;
    }

    @Transactional
    public void deleteAccount(int id) {
        accountRepository.deleteById(id);
    }
}
