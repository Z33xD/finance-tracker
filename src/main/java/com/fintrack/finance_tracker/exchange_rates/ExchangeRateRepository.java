package com.fintrack.finance_tracker.exchange_rates;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Integer> {
    void deleteById(int id);
    Optional<ExchangeRate> findById(int id);
}
