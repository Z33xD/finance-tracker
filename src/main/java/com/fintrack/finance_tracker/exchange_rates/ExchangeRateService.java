package com.fintrack.finance_tracker.exchange_rates;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ExchangeRateService {
    private final WebClient webClient;
    private final ExchangeRateRepository exchangeRateRepository;

    @Value("${exchangeRate.api.key}")
    private String apiKey;

    private final Set<String> refreshedToday = ConcurrentHashMap.newKeySet();

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
        return exchangeRateRepository.findByPairAndDate(
                Currencies.normalize(from_currency),
                Currencies.normalize(to_currency),
                date
        );
    }

    public List<ExchangeRate> getExchangeRatesByDate(LocalDate date) {
        return exchangeRateRepository.findByDate(date);
    }

    public ExchangeRate addExchangeRate(ExchangeRate exchangeRate) {
        exchangeRate.setFrom_currency(Currencies.normalize(exchangeRate.getFrom_currency()));
        exchangeRate.setTo_currency(Currencies.normalize(exchangeRate.getTo_currency()));
        exchangeRateRepository.save(exchangeRate);
        if (exchangeRate.getCreated_at() == null) {
            exchangeRate.setCreated_at(LocalDateTime.now());
        }
        return exchangeRate;
    }

    public ExchangeRate updateExchangeRate(int id, ExchangeRate updatedExchangeRate) {
        Optional<ExchangeRate> existingExchangeRate = exchangeRateRepository.findById(id);

        if (existingExchangeRate.isPresent()) {
            ExchangeRate exchangeRateToUpdate = existingExchangeRate.get();

            if (updatedExchangeRate.getFrom_currency() != null) {
                exchangeRateToUpdate.setFrom_currency(Currencies.normalize(updatedExchangeRate.getFrom_currency()));
            }
            if (updatedExchangeRate.getTo_currency() != null) {
                exchangeRateToUpdate.setTo_currency(Currencies.normalize(updatedExchangeRate.getTo_currency()));
            }

            exchangeRateToUpdate.setRate(updatedExchangeRate.getRate());

            if (updatedExchangeRate.getDate() != null) {
                exchangeRateToUpdate.setDate(updatedExchangeRate.getDate());
            }

            exchangeRateRepository.save(exchangeRateToUpdate);
            return exchangeRateToUpdate;
        }
        return null;
    }

    @Transactional
    public void deleteExchangeRate(int id) {
        exchangeRateRepository.deleteById(id);
    }

    /**
     * Resolves the multiplier to convert an amount in {@code from} into {@code to} for the
     * given {@code date}. Uses a direct stored pair first, then derives a cross-rate through
     * the {@code anchor} currency, then falls back to the most recently stored pair.
     */
    public double getRate(String from, String to, String anchor, LocalDate date) {
        String f = Currencies.normalize(from);
        String t = Currencies.normalize(to);
        String a = Currencies.normalize(anchor);

        if (f.equals(t)) {
            return 1.0;
        }

        Optional<ExchangeRate> direct = exchangeRateRepository.findByPairAndDate(f, t, date);
        if (direct.isPresent()) {
            return direct.get().getRate();
        }

        Double toLeg = anchorRate(a, t, date);
        Double fromLeg = anchorRate(a, f, date);
        if (toLeg != null && fromLeg != null) {
            return toLeg / fromLeg;
        }

        Optional<ExchangeRate> recentDirect = exchangeRateRepository.findMostRecentPair(f, t);
        if (recentDirect.isPresent()) {
            return recentDirect.get().getRate();
        }

        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Exchange rate unavailable: " + f + " -> " + t + " on " + date);
    }

    /**
     * Fetches the full set of rates for a base currency from the exchange rate API and
     * upserts every pair (base -> X) for the given date. One API call populates all pairs.
     */
    @Transactional
    public void refreshAllRates(String base, LocalDate date) {
        String b = Currencies.normalize(base);
        String uri = String.format("/%s/latest/%s", apiKey, b);

        Map<String, Object> response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        if (response == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No response from exchange rate API");
        }

        Object conversionRates = response.get("conversion_rates");
        if (!(conversionRates instanceof Map)) {
            String error = String.valueOf(response.get("error-type"));
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Exchange rate API error: " + error);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> rates = (Map<String, Object>) conversionRates;

        rates.forEach((toCurrency, rate) -> {
            double rateValue = (rate instanceof Number) ? ((Number) rate).doubleValue() : Double.parseDouble(String.valueOf(rate));
            upsert(b, toCurrency.toUpperCase(), rateValue, date);
        });
    }

    public ExchangeRate refreshRate(String base, String target, LocalDate date) {
        refreshAllRates(base, date);
        return exchangeRateRepository.findByPairAndDate(
                Currencies.normalize(base),
                Currencies.normalize(target),
                date
        ).orElse(null);
    }

    private Double anchorRate(String anchor, String target, LocalDate date) {
        if (anchor.equals(target)) {
            return 1.0;
        }
        Optional<ExchangeRate> pair = exchangeRateRepository.findByPairAndDate(anchor, target, date);
        if (pair.isPresent()) {
            return pair.get().getRate();
        }

        tryRefresh(anchor, date);

        return exchangeRateRepository.findByPairAndDate(anchor, target, date)
                .or(() -> exchangeRateRepository.findMostRecentPair(anchor, target))
                .map(ExchangeRate::getRate)
                .orElse(null);
    }

    private void tryRefresh(String anchor, LocalDate date) {
        String key = anchor + "|" + date;
        if (!refreshedToday.add(key)) {
            return;
        }
        try {
            refreshAllRates(anchor, date);
        } catch (Exception e) {
            System.out.println("Exchange rate refresh failed for " + anchor + ": " + e.getMessage());
        }
    }

    private void upsert(String from, String to, double rate, LocalDate date) {
        exchangeRateRepository.findByPairAndDate(from, to, date)
                .map(existing -> {
                    existing.setRate(rate);
                    return exchangeRateRepository.save(existing);
                })
                .orElseGet(() -> exchangeRateRepository.save(
                        new ExchangeRate(from, to, rate, date, LocalDateTime.now())
                ));
    }
}
