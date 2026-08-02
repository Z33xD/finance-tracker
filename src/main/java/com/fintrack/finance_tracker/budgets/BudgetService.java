package com.fintrack.finance_tracker.budgets;

import com.fintrack.finance_tracker.accounts.Account;
import com.fintrack.finance_tracker.accounts.AccountRepository;
import com.fintrack.finance_tracker.exchange_rates.BaseCurrencyResolver;
import com.fintrack.finance_tracker.exchange_rates.Currencies;
import com.fintrack.finance_tracker.exchange_rates.CurrencyConversionService;
import com.fintrack.finance_tracker.transactions.Transaction;
import com.fintrack.finance_tracker.transactions.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CurrencyConversionService currencyConversionService;
    private final BaseCurrencyResolver baseCurrencyResolver;

    public BudgetService(BudgetRepository budgetRepository,
                         AccountRepository accountRepository,
                         TransactionRepository transactionRepository,
                         CurrencyConversionService currencyConversionService,
                         BaseCurrencyResolver baseCurrencyResolver) {
        this.budgetRepository = budgetRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.currencyConversionService = currencyConversionService;
        this.baseCurrencyResolver = baseCurrencyResolver;
    }

    public List<Budget> getBudgets() {
        return budgetRepository.findAll();
    }

    public Optional<Budget> getBudgetById(int id) {
        return budgetRepository.findById(id);
    }

    public List<Budget> getBudgetsByMonthAndYear(int month, int year) {
        return budgetRepository.findAll().stream()
                .filter(budget -> (budget.getMonth() == month && budget.getYear() == year))
                .collect(Collectors.toList());
    }

    public List<Budget> getBudgetsByUserIdAndMonthAndYear(int userId, int month, int year) {
        return budgetRepository.findAll().stream()
                .filter(budget -> (budget.getUser_id() == userId && budget.getMonth() == month && budget.getYear() == year))
                .collect(Collectors.toList());
    }

    public List<Budget> getBudgetsByUserId(int userId) {
        return budgetRepository.findAll().stream()
                .filter(budget -> (budget.getUser_id() == userId))
                .collect(Collectors.toList());
    }

    public Budget addBudget(Budget budget) {
        if (budget.getCurrency() == null || budget.getCurrency().isBlank()) {
            budget.setCurrency("INR");
        } else {
            budget.setCurrency(budget.getCurrency().toUpperCase());
        }
        budgetRepository.save(budget);
        if (budget.getCreated_at() == null) {
            budget.setCreated_at(LocalDateTime.now());
        }
        return budget;
    }

    public Budget updateBudget(int id, Budget updatedBudget) {
        Optional<Budget> existingBudget = budgetRepository.findById(id);

        if (existingBudget.isPresent()) {
            Budget budgetToUpdate = existingBudget.get();
            budgetToUpdate.setUser_id(updatedBudget.getUser_id());
            budgetToUpdate.setCategory_id(updatedBudget.getCategory_id());
            budgetToUpdate.setAmount(updatedBudget.getAmount());
            if (updatedBudget.getCurrency() != null) {
                budgetToUpdate.setCurrency(updatedBudget.getCurrency().toUpperCase());
            }
            budgetToUpdate.setMonth(updatedBudget.getMonth());
            budgetToUpdate.setYear(updatedBudget.getYear());

            budgetRepository.save(budgetToUpdate);
            return budgetToUpdate;
        }
        return null;
    }

    @Transactional
    public void deleteBudget(int id) {
        budgetRepository.deleteById(id);
    }

    /**
     * Computes, for every budget of the user, the actual spent amount in the budget's own
     * currency for that budget's month/year. Each expense transaction is converted from its
     * account's currency into the budget currency using the exchange rate for its own date.
     */
    public List<BudgetSummary> getBudgetSummaries(int userId) {
        List<Budget> budgets = getBudgetsByUserId(userId);
        if (budgets.isEmpty()) {
            return List.of();
        }

        String base = baseCurrencyResolver.resolveForUser(userId);

        Map<Integer, String> accountCurrencies = accountRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(Account::getId, account -> Currencies.normalize(account.getCurrency()), (x, y) -> x));

        Map<String, List<Budget>> byPeriod = budgets.stream()
                .collect(Collectors.groupingBy(
                        budget -> budget.getYear() + "-" + String.format("%02d", budget.getMonth()),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<BudgetSummary> result = new ArrayList<>();
        for (Map.Entry<String, List<Budget>> entry : byPeriod.entrySet()) {
            String[] parts = entry.getKey().split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);

            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            List<Transaction> periodTransactions = accountCurrencies.isEmpty()
                    ? List.of()
                    : transactionRepository
                            .findByAccountIdInAndTransactionDateBetween(new ArrayList<>(accountCurrencies.keySet()), start, end);

            for (Budget budget : entry.getValue()) {
                String budgetCurrency = Currencies.normalize(budget.getCurrency());
                double spent = 0;

                for (Transaction transaction : periodTransactions) {
                    if (transaction.getCategoryId() == null || transaction.getCategoryId() != budget.getCategory_id()) {
                        continue;
                    }
                    if (!isExpense(transaction.getTransactionType())) {
                        continue;
                    }
                    String fromCurrency = accountCurrencies.getOrDefault(transaction.getAccount_id(), "INR");
                    spent += currencyConversionService.convert(
                            transaction.getAmount(), fromCurrency, budgetCurrency,
                            transaction.getTransactionDate(), base
                    );
                }

                result.add(new BudgetSummary(
                        budget.getId(), budget.getCategory_id(), budgetCurrency,
                        budget.getAmount(), spent, budget.getAmount() - spent
                ));
            }
        }
        return result;
    }

    private boolean isExpense(String transactionType) {
        return transactionType != null
                && (transactionType.equalsIgnoreCase("EXPENSE") || transactionType.equalsIgnoreCase("DEBIT"));
    }
}
