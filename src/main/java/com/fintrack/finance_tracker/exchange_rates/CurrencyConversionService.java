package com.fintrack.finance_tracker.exchange_rates;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class CurrencyConversionService {
    private final ExchangeRateService exchangeRateService;

    public CurrencyConversionService(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    /**
     * Converts an amount from one currency into another using the exchange rate for the
     * given date, deriving cross-rates through the {@code anchor} currency.
     */
    public double convert(double amount, String from, String to, LocalDate date, String anchor) {
        return amount * exchangeRateService.getRate(from, to, anchor, date);
    }
}
