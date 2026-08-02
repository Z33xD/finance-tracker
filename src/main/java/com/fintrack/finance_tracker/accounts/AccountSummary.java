package com.fintrack.finance_tracker.accounts;

import java.util.List;

public record AccountSummary(String base, double total, List<AccountSummaryItem> accounts) {
}
