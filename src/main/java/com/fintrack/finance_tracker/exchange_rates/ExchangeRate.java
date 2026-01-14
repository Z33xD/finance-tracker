package com.fintrack.finance_tracker.exchange_rates;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_rates")
public class ExchangeRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    private String from_currency;

    private String to_currency;

    private double rate;

    private LocalDate date;

    private LocalDateTime created_at;

    public ExchangeRate(int id, String from_currency, String to_currency, double rate, LocalDate date, LocalDateTime created_at) {
        this.id = id;
        this.from_currency = from_currency;
        this.to_currency = to_currency;
        this.rate = rate;
        this.date = date;
        this.created_at = created_at;
    }

    public ExchangeRate(String from_currency, String to_currency, double rate, LocalDate date, LocalDateTime created_at) {
        this.from_currency = from_currency;
        this.to_currency = to_currency;
        this.rate = rate;
        this.date = date;
        this.created_at = created_at;
    }

    public ExchangeRate() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFrom_currency() {
        return from_currency;
    }

    public void setFrom_currency(String from_currency) {
        this.from_currency = from_currency;
    }

    public String getTo_currency() {
        return to_currency;
    }

    public void setTo_currency(String to_currency) {
        this.to_currency = to_currency;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }
}
