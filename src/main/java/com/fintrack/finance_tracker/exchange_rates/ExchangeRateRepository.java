package com.fintrack.finance_tracker.exchange_rates;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Integer> {
    void deleteById(int id);
    Optional<ExchangeRate> findById(int id);

    @Query("SELECT e FROM ExchangeRate e WHERE e.from_currency = :from AND e.to_currency = :to AND e.date = :date")
    Optional<ExchangeRate> findByPairAndDate(@Param("from") String from, @Param("to") String to, @Param("date") LocalDate date);

    @Query("SELECT e FROM ExchangeRate e WHERE e.date = :date")
    List<ExchangeRate> findByDate(@Param("date") LocalDate date);

    @Query(value = "SELECT * FROM exchange_rates WHERE from_currency = :from AND to_currency = :to ORDER BY date DESC LIMIT 1", nativeQuery = true)
    Optional<ExchangeRate> findMostRecentPair(@Param("from") String from, @Param("to") String to);
}
