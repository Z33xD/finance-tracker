package com.fintrack.finance_tracker.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table (name="accounts")
public class Account {
    @Id
    @Column (name = "id", unique = true)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "user_id")
    @JsonProperty("user_id")
    private int userId;

    @Column(name = "account_name")
    @JsonProperty("account_name")
    private String accountName;

    @Column(name = "account_type")
    @JsonProperty("account_type")
    private String accountType;

    private String currency;

    @Column(name = "initial_balance")
    @JsonProperty("initial_balance")
    private double initialBalance;

    @Column(name = "created_at")
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public Account(int id, int userId, String accountName, String accountType, String currency, double initialBalance, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.accountName = accountName;
        this.accountType = accountType;
        this.currency = currency;
        this.initialBalance = initialBalance;
        this.createdAt = createdAt;
    }

    public Account() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(double initialBalance) {
        this.initialBalance = initialBalance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
