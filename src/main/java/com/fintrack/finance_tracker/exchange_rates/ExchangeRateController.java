package com.fintrack.finance_tracker.exchange_rates;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(path = "/api/exchange-rates")
public class ExchangeRateController {
    private final ExchangeRateService exchangeRateService;

    @Autowired
    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping
    public List<ExchangeRate> getExchangeRates(
            @RequestParam (required = false) Integer id,
            @RequestParam (required = false) String from_currency,
            @RequestParam (required = false) String to_currency,
            @RequestParam (required = false) LocalDate date
            ) {
        if (id != null) {
            return exchangeRateService.getExchangeRateById(id)
                    .map(List::of)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        }
        else if ((from_currency != null) && (to_currency != null) && (date != null)) {
            return exchangeRateService.getExchangeRatesByFromToAndDate(from_currency, to_currency, date)
                    .map(List::of)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        }
        else if (date != null) {
            return exchangeRateService.getExchangeRatesByDate(date);
        }
        else {
            return exchangeRateService.getExchangeRates();
        }
    }

    @GetMapping("/{baseCurrency}/{targetCurrency}")
    public ResponseEntity<ExchangeRate> getCurrencyRateToday(@PathVariable String baseCurrency, @PathVariable String targetCurrency) {
        LocalDate today = LocalDate.now();
        return exchangeRateService.getExchangeRatesByFromToAndDate(baseCurrency, targetCurrency, today)
                .map(exchangeRate -> new ResponseEntity<>(exchangeRate, HttpStatus.OK))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{baseCurrency}/{targetCurrency}/{date}")
    public ResponseEntity<ExchangeRate> getCurrencyRate(@PathVariable String baseCurrency, @PathVariable String targetCurrency, @PathVariable LocalDate date) {
        return exchangeRateService.getExchangeRatesByFromToAndDate(baseCurrency, targetCurrency, date)
                .map(exchangeRate -> new ResponseEntity<>(exchangeRate, HttpStatus.OK))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<ExchangeRate> addExchangeRate(@RequestBody ExchangeRate exchangeRate) {
        ExchangeRate createdExchangeRate = exchangeRateService.addExchangeRate(exchangeRate);
        return new ResponseEntity<>(createdExchangeRate, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExchangeRate> updateExchangeRateById(@PathVariable int id, @RequestBody ExchangeRate exchangeRate) {
        ExchangeRate updatedExchangeRate = exchangeRateService.updateExchangeRate(id, exchangeRate);
        if (updatedExchangeRate != null) {
            return new ResponseEntity<>(updatedExchangeRate, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteExchangeRate(@PathVariable int id) {
        exchangeRateService.deleteExchangeRate(id);
        return new ResponseEntity<>("Exchange rate deleted successfully!", HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ExchangeRate> refreshTodayRate(
            @RequestParam String base,
            @RequestParam String target
    ) {
        ExchangeRate refreshed = exchangeRateService.refreshRate(base, target, LocalDate.now());
        return new ResponseEntity<>(refreshed, HttpStatus.OK);
    }
}
