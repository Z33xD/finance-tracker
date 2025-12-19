package com.fintrack.finance_tracker.accounts;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table (name="accounts")
public class Account {
    @Id
    @Column (name = "id", unique = true)
    private int id;

    private int user_id;

    private String account_name;

    private String account_type;

    private String currency;

    private double initial_balance;

    private LocalDateTime created_at;

    public Account(int id, int user_id, String account_name, String account_type, String currency, double initial_balance, LocalDateTime created_at) {
        this.id = id;
        this.user_id = user_id;
        this.account_name = account_name;
        this.account_type = account_type;
        this.currency = currency;
        this.initial_balance = initial_balance;
        this.created_at = created_at;
    }

    public Account() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getAccount_name() {
        return account_name;
    }

    public void setAccount_name(String account_name) {
        this.account_name = account_name;
    }

    public String getAccount_type() {
        return account_type;
    }

    public void setAccount_type(String account_type) {
        this.account_type = account_type;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getInitial_balance() {
        return initial_balance;
    }

    public void setInitial_balance(double initial_balance) {
        this.initial_balance = initial_balance;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }
}
