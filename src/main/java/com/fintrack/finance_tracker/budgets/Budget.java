package com.fintrack.finance_tracker.budgets;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "budgets")
public class Budget {
    @Id
    @Column(name = "id", unique = true)
    private int id;

    private int user_id;

    private int category_id;

    private double amount;

    private int month;

    private int year;

    private LocalDateTime created_at;

    public Budget(int id, int user_id, int category_id, double amount, int month, int year, LocalDateTime created_at) {
        this.id = id;
        this.user_id = user_id;
        this.category_id = category_id;
        this.amount = amount;
        this.month = month;
        this.year = year;
        this.created_at = created_at;
    }

    public Budget() {}

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

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }
}
