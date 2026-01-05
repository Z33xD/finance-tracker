package com.fintrack.finance_tracker.exchange_rates;

import jakarta.transaction.Transactional;
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

    public ExchangeRate updateExchangeRate (int id, ExchangeRate updatedExchangeRate) {
        Optional<ExchangeRate> existingExchangeRate = exchangeRateRepository.findById(id);

        if (existingExchangeRate.isPresent()) {
            ExchangeRate exchangeRateToUpdate = existingExchangeRate.get();

            if (updatedExchangeRate.getFrom_currency() != null) {
                exchangeRateToUpdate.setFrom_currency(updatedExchangeRate.getFrom_currency());
            }
            if (updatedExchangeRate.getTo_currency() != null) {
                exchangeRateToUpdate.setTo_currency(updatedExchangeRate.getTo_currency());
            }

            exchangeRateToUpdate.setRate(updatedExchangeRate.getRate());

            if (updatedExchangeRate.getDate() != null) {
                exchangeRateToUpdate.setDate(updatedExchangeRate.getDate());
            }

            exchangeRateRepository.save(exchangeRateToUpdate);
        }
        return null;
    }

    @Transactional
    public void deleteExchangeRate(int id) {
        exchangeRateRepository.deleteById(id);
    }
}
