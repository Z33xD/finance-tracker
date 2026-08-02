package com.fintrack.finance_tracker.exchange_rates;

import com.fintrack.finance_tracker.accounts.Account;
import com.fintrack.finance_tracker.accounts.AccountRepository;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class BaseCurrencyResolver {
    private final AccountRepository accountRepository;

    public BaseCurrencyResolver(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Derives the reporting currency for a user from their earliest-created account,
     * falling back to INR when no accounts (or no currency) exist.
     */
    public String resolveForUser(int userId) {
        return accountRepository.findByUserId(userId).stream()
                .filter(account -> account.getCurrency() != null && !account.getCurrency().isBlank())
                .min(Comparator.comparing(Account::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(account -> Currencies.normalize(account.getCurrency()))
                .orElse("INR");
    }
}
