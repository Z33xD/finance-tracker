package com.fintrack.finance_tracker.exchange_rates;

public final class Currencies {
    private Currencies() {}

    public static String normalize(String code) {
        return (code == null || code.isBlank()) ? "INR" : code.trim().toUpperCase();
    }
}
