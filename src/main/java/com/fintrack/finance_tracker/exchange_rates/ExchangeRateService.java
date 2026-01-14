package com.fintrack.finance_tracker.exchange_rates;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ExchangeRateService {
    private final WebClient webClient;
    private final ExchangeRateRepository exchangeRateRepository;

    @Value("${exchangeRate.api.key}")
    private String apiKey;

    @Autowired
    public ExchangeRateService(WebClient webClient, ExchangeRateRepository exchangeRateRepository) {
        this.webClient = webClient;
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
        if (exchangeRate.getCreated_at() == null) {
            exchangeRate.setCreated_at(LocalDateTime.now());
        }
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

    public ExchangeRate refreshRate(String base, String target, LocalDate date) {
        String uri = String.format("/%s/latest/%s", apiKey, base);

        JsonNode response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        assert response != null;
        double rate = response
                .path("conversion_rates")
                .path(target)
                .doubleValue();

        return getExchangeRatesByFromToAndDate(base, target, date)
                .map(existing -> {
                    existing.setRate(rate);
                    return exchangeRateRepository.save(existing);
                })
                .orElseGet(() -> exchangeRateRepository.save(
                        new ExchangeRate(base, target, rate, date, LocalDateTime.now())
                ));
    }
}
