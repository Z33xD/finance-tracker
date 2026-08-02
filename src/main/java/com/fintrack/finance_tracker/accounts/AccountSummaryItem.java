package com.fintrack.finance_tracker.accounts;

public record AccountSummaryItem(int id, String accountName, String currency, double balance, double convertedBalance) {
}
