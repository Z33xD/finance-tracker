package com.fintrack.finance_tracker.exchange_rates;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ExchangeRateService {
    private final ExchangeRateRepository exchangeRateRepository;

    @Autowired
    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    public List<ExchangeRate> getExchangeRates() {
        return exchangeRateRepository.findAll();
    }

    public Optional<ExchangeRate> getExchangeRateById(int id) {
        return exchangeRateRepository.findById(id);
    }

    public Optional<ExchangeRate> getExchangeRatesByFromToAndDate(String from_currency, String to_currency, LocalDate date) {
        return exchangeRateRepository.findAll().stream()
                .filter(exchangeRate -> (exchangeRate.getFrom_currency().equalsIgnoreCase(from_currency)
                        && exchangeRate.getTo_currency().equalsIgnoreCase(to_currency)
                        && exchangeRate.getDate().equals(date)))
                .findFirst();
    }

    public List<ExchangeRate> getExchangeRatesByDate(LocalDate date) {
        return exchangeRateRepository.findAll().stream()
                .filter(exchangeRate -> (exchangeRate.getDate().equals(date)))
                .collect(Collectors.toList());
    }

    public ExchangeRate addExchangeRate (ExchangeRate exchangeRate) {
        exchangeRateRepository.save(exchangeRate);
        return exchangeRate;
    }

    public ExchangeRate updateExchangeRate (ExchangeRate updatedExchangeRate) {
        Optional<ExchangeRate> existingExchangeRate = exchangeRateRepository.findById(updatedExchangeRate.getId());

        if (existingExchangeRate.isPresent()) {
            ExchangeRate exchangeRateToUpdate = existingExchangeRate.get();
            exchangeRateToUpdate.setFrom_currency(updatedExchangeRate.getFrom_currency());
            exchangeRateToUpdate.setTo_currency(updatedExchangeRate.getTo_currency());
            exchangeRateToUpdate.setRate(updatedExchangeRate.getRate());
            exchangeRateToUpdate.setDate(updatedExchangeRate.getDate());

            exchangeRateRepository.save(exchangeRateToUpdate);
        }
        return null;
    }
}
