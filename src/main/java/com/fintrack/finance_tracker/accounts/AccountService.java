package com.fintrack.finance_tracker.accounts;

import com.fintrack.finance_tracker.exchange_rates.BaseCurrencyResolver;
import com.fintrack.finance_tracker.exchange_rates.Currencies;
import com.fintrack.finance_tracker.exchange_rates.CurrencyConversionService;
import com.fintrack.finance_tracker.transactions.Transaction;
import com.fintrack.finance_tracker.transactions.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CurrencyConversionService currencyConversionService;
    private final BaseCurrencyResolver baseCurrencyResolver;

    @Autowired
    public AccountService(AccountRepository accountRepository,
                          TransactionRepository transactionRepository,
                          CurrencyConversionService currencyConversionService,
                          BaseCurrencyResolver baseCurrencyResolver) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.currencyConversionService = currencyConversionService;
        this.baseCurrencyResolver = baseCurrencyResolver;
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
        if (account.getCurrency() == null || account.getCurrency().isBlank()) {
            account.setCurrency("INR");
        } else if (!account.getCurrency().matches("[A-Za-z]{3}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Currency must be a 3-letter ISO code");
        } else {
            account.setCurrency(account.getCurrency().toUpperCase());
        }
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
                if (!updatedAccount.getCurrency().matches("[A-Za-z]{3}")) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Currency must be a 3-letter ISO code");
                }
                accountToUpdate.setCurrency(updatedAccount.getCurrency().toUpperCase());
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

    /**
     * Summarises all of a user's accounts, converting each balance into the reporting
     * currency (derived from the user's first account, unless overridden).
     */
    public AccountSummary getAccountSummary(int userId, String baseOverride) {
        String base = Currencies.normalize(
                (baseOverride != null && !baseOverride.isBlank()) ? baseOverride : baseCurrencyResolver.resolveForUser(userId)
        );

        List<Account> accounts = getAccountsByUserId(userId);
        List<AccountSummaryItem> items = new ArrayList<>();
        double total = 0;

        for (Account account : accounts) {
            double balance = account.getBalance() != null ? account.getBalance() : account.getInitialBalance();
            String currency = Currencies.normalize(account.getCurrency());
            double converted = currencyConversionService.convert(balance, currency, base, LocalDate.now(), base);
            total += converted;
            items.add(new AccountSummaryItem(account.getId(), account.getAccountName(), currency, balance, converted));
        }

        return new AccountSummary(base, total, items);
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
