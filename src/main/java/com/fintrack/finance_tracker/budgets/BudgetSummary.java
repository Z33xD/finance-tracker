package com.fintrack.finance_tracker.budgets;

public record BudgetSummary(int id, int categoryId, String currency, double amount, double spent, double remaining) {
}
