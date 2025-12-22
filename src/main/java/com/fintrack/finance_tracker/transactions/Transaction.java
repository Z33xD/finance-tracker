package com.fintrack.finance_tracker.transactions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @Column(name = "id", unique = true)
    private int id;

    private int account_id;

    private int category_id;

    private double amount;

    private LocalDate transaction_date;

    private String description;

    private String transaction_type;

    private int import_batch_id;

    private LocalDateTime datetime;

    public Transaction(int id, int account_id, int category_id, double amount, LocalDate transaction_date, String description, String transaction_type, int import_batch_id, LocalDateTime datetime) {
        this.id = id;
        this.account_id = account_id;
        this.category_id = category_id;
        this.amount = amount;
        this.transaction_date = transaction_date;
        this.description = description;
        this.transaction_type = transaction_type;
        this.import_batch_id = import_batch_id;
        this.datetime = datetime;
    }

    public Transaction() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAccount_id() {
        return account_id;
    }

    public void setAccount_id(int account_id) {
        this.account_id = account_id;
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

    public LocalDate getTransaction_date() {
        return transaction_date;
    }

    public void setTransaction_date(LocalDate transaction_date) {
        this.transaction_date = transaction_date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTransaction_type() {
        return transaction_type;
    }

    public void setTransaction_type(String transaction_type) {
        this.transaction_type = transaction_type;
    }

    public int getImport_batch_id() {
        return import_batch_id;
    }

    public void setImport_batch_id(int import_batch_id) {
        this.import_batch_id = import_batch_id;
    }

    public LocalDateTime getDatetime() {
        return datetime;
    }

    public void setDatetime(LocalDateTime datetime) {
        this.datetime = datetime;
    }
}
