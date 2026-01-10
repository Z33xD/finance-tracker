package com.fintrack.finance_tracker.budgets;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class BudgetService {
    private final BudgetRepository budgetRepository;

    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
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

    public Budget addBudget(Budget budget) {
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
}
